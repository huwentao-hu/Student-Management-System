package com.example.studentmanagement.course;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.studentmanagement.auth.AccessDeniedException;
import com.example.studentmanagement.auth.AuthenticatedUser;
import com.example.studentmanagement.auth.AuthenticationInterceptor;
import com.example.studentmanagement.auth.UserRole;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@RestController
@Validated
@RequestMapping("/api/students/{studentId}/timetable")
public class StudentTimetableController {

	private final StudentTimetableService studentTimetableService;

	public StudentTimetableController(StudentTimetableService studentTimetableService) {
		this.studentTimetableService = studentTimetableService;
	}

	@GetMapping
	public StudentTimetableResponse get(
			@PathVariable long studentId,
			@RequestParam @Min(1900) @Max(2200) int academicYear,
			@RequestParam @NotNull Semester semester,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		if (user.role() == UserRole.STUDENT && !Long.valueOf(studentId).equals(user.studentId())) {
			throw new AccessDeniedException("Students can only view their own timetable");
		}
		return studentTimetableService.get(studentId, academicYear, semester);
	}
}
