package com.example.studentmanagement.attendance;

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
@RequestMapping("/api/attendance-statistics")
public class AttendanceStatisticsController {

	private final AttendanceStatisticsService attendanceStatisticsService;

	public AttendanceStatisticsController(AttendanceStatisticsService attendanceStatisticsService) {
		this.attendanceStatisticsService = attendanceStatisticsService;
	}

	@GetMapping("/course-offerings/{courseOfferingId}")
	public CourseOfferingAttendanceStatisticsResponse getCourseOfferingStatistics(@PathVariable long courseOfferingId,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		return attendanceStatisticsService.getCourseOfferingStatistics(courseOfferingId, user);
	}

	@GetMapping("/students/{studentId}")
	public StudentTermAttendanceStatisticsResponse getStudentTermStatistics(@PathVariable long studentId,
			@RequestParam @Min(1900) @Max(2200) int academicYear, @RequestParam Semester semester,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		return attendanceStatisticsService.getStudentTermStatistics(studentId, academicYear, semester, user);
	}
}
