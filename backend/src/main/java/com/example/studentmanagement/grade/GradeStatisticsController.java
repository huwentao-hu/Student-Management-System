package com.example.studentmanagement.grade;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.studentmanagement.auth.AuthenticatedUser;
import com.example.studentmanagement.auth.AuthenticationInterceptor;
import com.example.studentmanagement.course.Semester;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@Validated
@RequestMapping("/api/grade-statistics")
public class GradeStatisticsController {

	private final GradeStatisticsService gradeStatisticsService;

	public GradeStatisticsController(GradeStatisticsService gradeStatisticsService) {
		this.gradeStatisticsService = gradeStatisticsService;
	}

	@GetMapping("/course-offerings/{courseOfferingId}")
	public CourseOfferingGradeStatisticsResponse getCourseOfferingStatistics(@PathVariable long courseOfferingId,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		return gradeStatisticsService.getCourseOfferingStatistics(courseOfferingId, user);
	}

	@GetMapping("/students/{studentId}")
	public StudentTermGradeStatisticsResponse getStudentTermStatistics(@PathVariable long studentId,
			@RequestParam @Min(1900) @Max(2200) int academicYear, @RequestParam Semester semester,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		return gradeStatisticsService.getStudentTermStatistics(studentId, academicYear, semester, user);
	}
}
