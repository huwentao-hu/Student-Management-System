package com.example.studentmanagement.grade;

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
public class GradeStatisticsService {

	private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

	private final GradeRepository gradeRepository;
	private final CourseOfferingRepository courseOfferingRepository;
	private final StudentRepository studentRepository;

	public GradeStatisticsService(GradeRepository gradeRepository, CourseOfferingRepository courseOfferingRepository,
			StudentRepository studentRepository) {
		this.gradeRepository = gradeRepository;
		this.courseOfferingRepository = courseOfferingRepository;
		this.studentRepository = studentRepository;
	}

	public CourseOfferingGradeStatisticsResponse getCourseOfferingStatistics(long courseOfferingId,
			AuthenticatedUser user) {
		CourseOffering offering = courseOfferingRepository.findById(courseOfferingId)
			.orElseThrow(() -> new CourseOfferingNotFoundException(courseOfferingId));
		requireCanReadOfferingStatistics(user, offering);
		CourseOfferingGradeAggregate aggregate = gradeRepository.aggregateByCourseOfferingId(courseOfferingId);
		long gradedCount = aggregate.getGradedCount();
		if (gradedCount == 0) {
			return CourseOfferingGradeStatisticsResponse.from(offering, 0, null, null, null, 0, 0, null);
		}

		long passedCount = valueOrZero(aggregate.getPassedCount());
		long failedCount = gradedCount - passedCount;
		BigDecimal averageScore = BigDecimal.valueOf(aggregate.getAverageScore()).setScale(2, RoundingMode.HALF_UP);
		BigDecimal passRate = divide(BigDecimal.valueOf(passedCount).multiply(ONE_HUNDRED),
				BigDecimal.valueOf(gradedCount));
		return CourseOfferingGradeStatisticsResponse.from(offering, gradedCount, averageScore, aggregate.getHighestScore(),
				aggregate.getLowestScore(), passedCount, failedCount, passRate);
	}

	public StudentTermGradeStatisticsResponse getStudentTermStatistics(long studentId, int academicYear,
			Semester semester, AuthenticatedUser user) {
		Student student = studentRepository.findById(studentId)
			.orElseThrow(() -> new StudentNotFoundException("Student not found: " + studentId));
		requireCanReadStudentStatistics(user, studentId);
		StudentTermGradeAggregate aggregate = gradeRepository.aggregateByStudentTerm(studentId, academicYear, semester);
		long gradeCount = aggregate.getGradeCount();
		BigDecimal totalCredits = valueOrZero(aggregate.getTotalCredits());
		BigDecimal weightedScoreTotal = valueOrZero(aggregate.getWeightedScoreTotal());
		BigDecimal weightedAverage = totalCredits.signum() == 0 ? null : divide(weightedScoreTotal, totalCredits);
		long passedCount = valueOrZero(aggregate.getPassedCourseCount());
		return StudentTermGradeStatisticsResponse.from(student, academicYear, semester, gradeCount, totalCredits,
				weightedAverage, passedCount, gradeCount - passedCount);
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
			throw new AccessDeniedException("Students can only view their own term grade statistics");
		}
	}

	private long valueOrZero(Long value) {
		return value == null ? 0 : value;
	}

	private BigDecimal valueOrZero(BigDecimal value) {
		return value == null ? BigDecimal.ZERO : value;
	}

	private BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
		return dividend.divide(divisor, 2, RoundingMode.HALF_UP);
	}
}
