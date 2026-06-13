package com.example.studentmanagement.grade;

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
import com.example.studentmanagement.course.Semester;
import com.example.studentmanagement.schoolclass.StudentClassAssignmentRepository;
import com.example.studentmanagement.student.Student;
import com.example.studentmanagement.student.StudentNotFoundException;
import com.example.studentmanagement.student.StudentRepository;

import jakarta.persistence.criteria.Predicate;

@Service
@Transactional(readOnly = true)
public class GradeService {

	private final GradeRepository gradeRepository;
	private final StudentRepository studentRepository;
	private final CourseOfferingRepository courseOfferingRepository;
	private final StudentClassAssignmentRepository assignmentRepository;

	public GradeService(GradeRepository gradeRepository, StudentRepository studentRepository,
			CourseOfferingRepository courseOfferingRepository, StudentClassAssignmentRepository assignmentRepository) {
		this.gradeRepository = gradeRepository;
		this.studentRepository = studentRepository;
		this.courseOfferingRepository = courseOfferingRepository;
		this.assignmentRepository = assignmentRepository;
	}

	@Transactional
	public GradeUpsertResult upsert(UpsertGradeRequest request, AuthenticatedUser user) {
		Student student = studentRepository.findByIdForUpdate(request.studentId())
			.orElseThrow(() -> new StudentNotFoundException("Student not found: " + request.studentId()));
		CourseOffering offering = courseOfferingRepository.findById(request.courseOfferingId())
			.orElseThrow(() -> new CourseOfferingNotFoundException(request.courseOfferingId()));
		requireCanManage(user, offering);
		boolean attended = assignmentRepository.findOverlappingPeriod(student.getId(),
				AcademicTermDates.start(offering.getAcademicYear(), offering.getSemester()),
				AcademicTermDates.end(offering.getAcademicYear(), offering.getSemester()))
			.stream()
			.anyMatch(assignment -> assignment.getSchoolClass().getId().equals(offering.getSchoolClass().getId()));
		if (!attended) {
			throw new InvalidGradeException("Student was not assigned to the offering class during this academic term");
		}
		Grade grade = gradeRepository.findByStudentIdAndCourseOfferingId(student.getId(), offering.getId()).orElse(null);
		boolean created = grade == null;
		if (created) {
			grade = new Grade(student, offering, request.score());
		}
		else {
			grade.updateScore(request.score());
		}
		return new GradeUpsertResult(GradeResponse.from(gradeRepository.save(grade)), created);
	}

	public GradeResponse getById(long id, AuthenticatedUser user) {
		Grade grade = gradeRepository.findById(id).orElseThrow(() -> new GradeNotFoundException(id));
		requireCanRead(user, grade);
		return GradeResponse.from(grade);
	}

	public PageResponse<GradeResponse> search(Long studentId, Long courseOfferingId, Integer academicYear,
			Semester semester, int page, int size, AuthenticatedUser user) {
		if (user.role() == UserRole.STUDENT && studentId != null && !studentId.equals(user.studentId())) {
			throw new AccessDeniedException("Students can only view their own grades");
		}
		Long effectiveStudentId = user.role() == UserRole.STUDENT ? user.studentId() : studentId;
		Specification<Grade> specification = (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.conjunction();
			if (effectiveStudentId != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("student").get("id"), effectiveStudentId));
			}
			if (courseOfferingId != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("courseOffering").get("id"), courseOfferingId));
			}
			if (academicYear != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("courseOffering").get("academicYear"), academicYear));
			}
			if (semester != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("courseOffering").get("semester"), semester));
			}
			if (user.role() == UserRole.TEACHER) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("courseOffering").get("teacher").get("id"), user.userId()));
			}
			return predicate;
		};
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by("id").descending());
		return PageResponse.from(gradeRepository.findAll(specification, pageRequest).map(GradeResponse::from));
	}

	private void requireCanManage(AuthenticatedUser user, CourseOffering offering) {
		if (user.role() == UserRole.ADMIN) {
			return;
		}
		if (user.role() != UserRole.TEACHER || !offering.getTeacher().getId().equals(user.userId())) {
			throw new AccessDeniedException("Only administrators or the offering teacher can manage this grade");
		}
	}

	private void requireCanRead(AuthenticatedUser user, Grade grade) {
		if (user.role() == UserRole.STUDENT && !grade.getStudent().getId().equals(user.studentId())) {
			throw new AccessDeniedException("Students can only view their own grades");
		}
		if (user.role() == UserRole.TEACHER
				&& !grade.getCourseOffering().getTeacher().getId().equals(user.userId())) {
			throw new AccessDeniedException("Teachers can only view grades for their own course offerings");
		}
	}
}
