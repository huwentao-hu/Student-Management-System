package com.example.studentmanagement.attendance;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.studentmanagement.auth.AccessDeniedException;
import com.example.studentmanagement.auth.AuthenticatedUser;
import com.example.studentmanagement.auth.UserRole;
import com.example.studentmanagement.course.CourseOffering;
import com.example.studentmanagement.course.CourseOfferingNotFoundException;
import com.example.studentmanagement.course.CourseOfferingRepository;
import com.example.studentmanagement.course.Semester;
import com.example.studentmanagement.student.Student;
import com.example.studentmanagement.student.StudentNotFoundException;
import com.example.studentmanagement.student.StudentRepository;

@Service
@Transactional(readOnly = true)
public class AttendanceStatisticsService {

	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

	private final AttendanceRecordRepository recordRepository;
	private final AttendanceSessionRepository sessionRepository;
	private final CourseOfferingRepository courseOfferingRepository;
	private final StudentRepository studentRepository;

	public AttendanceStatisticsService(AttendanceRecordRepository recordRepository,
			AttendanceSessionRepository sessionRepository, CourseOfferingRepository courseOfferingRepository,
			StudentRepository studentRepository) {
		this.recordRepository = recordRepository;
		this.sessionRepository = sessionRepository;
		this.courseOfferingRepository = courseOfferingRepository;
		this.studentRepository = studentRepository;
	}

	public CourseOfferingAttendanceStatisticsResponse getCourseOfferingStatistics(long courseOfferingId,
			AuthenticatedUser user) {
		CourseOffering offering = courseOfferingRepository.findById(courseOfferingId)
			.orElseThrow(() -> new CourseOfferingNotFoundException(courseOfferingId));
		requireCanReadOfferingStatistics(user, offering);
		long sessionCount = sessionRepository.countByCourseOfferingId(courseOfferingId);
		AttendanceStatisticsSummary summary = summarize(recordRepository.aggregateByCourseOfferingId(courseOfferingId));
		return CourseOfferingAttendanceStatisticsResponse.from(offering, sessionCount, summary);
	}

	public StudentTermAttendanceStatisticsResponse getStudentTermStatistics(long studentId, int academicYear,
			Semester semester, AuthenticatedUser user) {
		Student student = studentRepository.findById(studentId)
			.orElseThrow(() -> new StudentNotFoundException("Student not found: " + studentId));
		requireCanReadStudentStatistics(user, studentId);
		return StudentTermAttendanceStatisticsResponse.from(student, academicYear, semester,
				summarize(recordRepository.aggregateByStudentTerm(studentId, academicYear, semester)));
	}

	private AttendanceStatisticsSummary summarize(AttendanceAggregate aggregate) {
		long recordedCount = valueOrZero(aggregate.getRecordedCount());
		long presentCount = valueOrZero(aggregate.getPresentCount());
		long lateCount = valueOrZero(aggregate.getLateCount());
		long excusedCount = valueOrZero(aggregate.getExcusedCount());
		long absentCount = valueOrZero(aggregate.getAbsentCount());
		BigDecimal attendanceRate = recordedCount == 0 ? null
				: BigDecimal.valueOf(presentCount + lateCount)
					.multiply(ONE_HUNDRED)
					.divide(BigDecimal.valueOf(recordedCount), 2, RoundingMode.HALF_UP);
		return new AttendanceStatisticsSummary(recordedCount, presentCount, lateCount, excusedCount, absentCount,
				attendanceRate);
	}

	private long valueOrZero(Long value) {
		return value == null ? 0 : value;
	}

	private void requireCanReadOfferingStatistics(AuthenticatedUser user, CourseOffering offering) {
		if (user.role() == UserRole.ADMIN) {
			return;
		}
		if (user.role() != UserRole.TEACHER || !offering.getTeacher().getId().equals(user.userId())) {
			throw new AccessDeniedException("Only administrators or the offering teacher can view these statistics");
		}
	}

	private void requireCanReadStudentStatistics(AuthenticatedUser user, long studentId) {
		if (user.role() == UserRole.ADMIN) {
			return;
		}
		if (user.role() != UserRole.STUDENT || user.studentId() == null || user.studentId() != studentId) {
			throw new AccessDeniedException("Students can only view their own term attendance statistics");
		}
	}
}
