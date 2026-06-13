package com.example.studentmanagement.attendance;

import java.time.LocalDate;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.studentmanagement.auth.AccessDeniedException;
import com.example.studentmanagement.auth.AuthenticatedUser;
import com.example.studentmanagement.auth.UserRole;
import com.example.studentmanagement.common.PageResponse;
import com.example.studentmanagement.course.AcademicTermDates;
import com.example.studentmanagement.course.CourseOffering;
import com.example.studentmanagement.course.CourseOfferingNotFoundException;
import com.example.studentmanagement.course.CourseOfferingRepository;
import com.example.studentmanagement.schoolclass.StudentClassAssignmentRepository;
import com.example.studentmanagement.student.Student;
import com.example.studentmanagement.student.StudentNotFoundException;
import com.example.studentmanagement.student.StudentRepository;

import jakarta.persistence.criteria.Predicate;

@Service
@Transactional(readOnly = true)
public class AttendanceService {

	private final AttendanceSessionRepository sessionRepository;
	private final AttendanceRecordRepository recordRepository;
	private final CourseOfferingRepository courseOfferingRepository;
	private final StudentRepository studentRepository;
	private final StudentClassAssignmentRepository assignmentRepository;

	public AttendanceService(AttendanceSessionRepository sessionRepository, AttendanceRecordRepository recordRepository,
			CourseOfferingRepository courseOfferingRepository, StudentRepository studentRepository,
			StudentClassAssignmentRepository assignmentRepository) {
		this.sessionRepository = sessionRepository;
		this.recordRepository = recordRepository;
		this.courseOfferingRepository = courseOfferingRepository;
		this.studentRepository = studentRepository;
		this.assignmentRepository = assignmentRepository;
	}

	@Transactional
	public AttendanceSessionResponse createSession(CreateAttendanceSessionRequest request, AuthenticatedUser user) {
		CourseOffering offering = courseOfferingRepository.findById(request.courseOfferingId())
			.orElseThrow(() -> new CourseOfferingNotFoundException(request.courseOfferingId()));
		requireCanManage(user, offering);
		if (request.sessionDate().isBefore(AcademicTermDates.start(offering.getAcademicYear(), offering.getSemester()))
				|| request.sessionDate()
					.isAfter(AcademicTermDates.end(offering.getAcademicYear(), offering.getSemester()))) {
			throw new InvalidAttendanceException("Attendance session date must be within the course offering term");
		}
		if (sessionRepository.existsByCourseOfferingIdAndSessionDate(offering.getId(), request.sessionDate())) {
			throw new DuplicateAttendanceSessionException();
		}
		return AttendanceSessionResponse.from(sessionRepository
			.save(new AttendanceSession(offering, request.sessionDate(), normalize(request.topic()))));
	}

	public AttendanceSessionResponse getSession(long id, AuthenticatedUser user) {
		AttendanceSession session = findSession(id);
		requireCanReadSession(user, session);
		return AttendanceSessionResponse.from(session);
	}

	public PageResponse<AttendanceSessionResponse> searchSessions(Long courseOfferingId, LocalDate sessionDate, int page,
			int size, AuthenticatedUser user) {
		if (user.role() == UserRole.STUDENT) {
			throw new AccessDeniedException("Students cannot access attendance session management");
		}
		Specification<AttendanceSession> specification = (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.conjunction();
			if (courseOfferingId != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("courseOffering").get("id"), courseOfferingId));
			}
			if (sessionDate != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("sessionDate"), sessionDate));
			}
			if (user.role() == UserRole.TEACHER) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("courseOffering").get("teacher").get("id"), user.userId()));
			}
			return predicate;
		};
		PageRequest pageRequest = PageRequest.of(page, size,
				Sort.by("sessionDate").descending().and(Sort.by("id").descending()));
		return PageResponse.from(sessionRepository.findAll(specification, pageRequest).map(AttendanceSessionResponse::from));
	}

	@Transactional
	public AttendanceUpsertResult upsertRecord(UpsertAttendanceRecordRequest request, AuthenticatedUser user) {
		AttendanceSession session = findSession(request.attendanceSessionId());
		requireCanManage(user, session.getCourseOffering());
		Student student = studentRepository.findByIdForUpdate(request.studentId())
			.orElseThrow(() -> new StudentNotFoundException("Student not found: " + request.studentId()));
		boolean assigned = assignmentRepository
			.findOverlappingPeriod(student.getId(), session.getSessionDate(), session.getSessionDate())
			.stream()
			.anyMatch(assignment -> assignment.getSchoolClass().getId()
				.equals(session.getCourseOffering().getSchoolClass().getId()));
		if (!assigned) {
			throw new InvalidAttendanceException("Student was not assigned to the offering class on the session date");
		}
		AttendanceRecord record = recordRepository.findByAttendanceSessionIdAndStudentId(session.getId(), student.getId())
			.orElse(null);
		boolean created = record == null;
		if (created) {
			record = new AttendanceRecord(session, student, request.status());
		}
		else {
			record.updateStatus(request.status());
		}
		return new AttendanceUpsertResult(AttendanceRecordResponse.from(recordRepository.save(record)), created);
	}

	public AttendanceRecordResponse getRecord(long id, AuthenticatedUser user) {
		AttendanceRecord record = recordRepository.findById(id).orElseThrow(() -> new AttendanceRecordNotFoundException(id));
		requireCanReadRecord(user, record);
		return AttendanceRecordResponse.from(record);
	}

	public PageResponse<AttendanceRecordResponse> searchRecords(Long studentId, Long courseOfferingId,
			Long attendanceSessionId, AttendanceStatus status, int page, int size, AuthenticatedUser user) {
		if (user.role() == UserRole.STUDENT && studentId != null && !studentId.equals(user.studentId())) {
			throw new AccessDeniedException("Students can only view their own attendance");
		}
		Long effectiveStudentId = user.role() == UserRole.STUDENT ? user.studentId() : studentId;
		Specification<AttendanceRecord> specification = (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.conjunction();
			if (effectiveStudentId != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("student").get("id"), effectiveStudentId));
			}
			if (courseOfferingId != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(
						root.get("attendanceSession").get("courseOffering").get("id"), courseOfferingId));
			}
			if (attendanceSessionId != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("attendanceSession").get("id"), attendanceSessionId));
			}
			if (status != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
			}
			if (user.role() == UserRole.TEACHER) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(
						root.get("attendanceSession").get("courseOffering").get("teacher").get("id"), user.userId()));
			}
			return predicate;
		};
		PageRequest pageRequest = PageRequest.of(page, size,
				Sort.by("attendanceSession.sessionDate").descending().and(Sort.by("id").descending()));
		return PageResponse.from(recordRepository.findAll(specification, pageRequest).map(AttendanceRecordResponse::from));
	}

	private AttendanceSession findSession(long id) {
		return sessionRepository.findById(id).orElseThrow(() -> new AttendanceSessionNotFoundException(id));
	}

	private void requireCanManage(AuthenticatedUser user, CourseOffering offering) {
		if (user.role() == UserRole.ADMIN) {
			return;
		}
		if (user.role() != UserRole.TEACHER || !offering.getTeacher().getId().equals(user.userId())) {
			throw new AccessDeniedException("Only administrators or the offering teacher can manage attendance");
		}
	}

	private void requireCanReadSession(AuthenticatedUser user, AttendanceSession session) {
		if (user.role() == UserRole.STUDENT) {
			throw new AccessDeniedException("Students cannot access attendance session management");
		}
		if (user.role() == UserRole.TEACHER
				&& !session.getCourseOffering().getTeacher().getId().equals(user.userId())) {
			throw new AccessDeniedException("Teachers can only view their own attendance sessions");
		}
	}

	private void requireCanReadRecord(AuthenticatedUser user, AttendanceRecord record) {
		if (user.role() == UserRole.STUDENT && !record.getStudent().getId().equals(user.studentId())) {
			throw new AccessDeniedException("Students can only view their own attendance");
		}
		if (user.role() == UserRole.TEACHER
				&& !record.getAttendanceSession().getCourseOffering().getTeacher().getId().equals(user.userId())) {
			throw new AccessDeniedException("Teachers can only view attendance for their own course offerings");
		}
	}

	private String normalize(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
