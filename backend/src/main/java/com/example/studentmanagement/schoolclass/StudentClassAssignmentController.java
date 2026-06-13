package com.example.studentmanagement.schoolclass;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.studentmanagement.auth.AuthenticatedUser;
import com.example.studentmanagement.auth.AuthenticationInterceptor;
import com.example.studentmanagement.student.StudentAuthorizationService;
import com.example.studentmanagement.student.StudentResponse;
import com.example.studentmanagement.student.StudentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/students/{studentId}/class-assignments")
public class StudentClassAssignmentController {

	private final StudentClassAssignmentService assignmentService;
	private final StudentAuthorizationService authorizationService;
	private final StudentService studentService;

	public StudentClassAssignmentController(StudentClassAssignmentService assignmentService,
			StudentAuthorizationService authorizationService, StudentService studentService) {
		this.assignmentService = assignmentService;
		this.authorizationService = authorizationService;
		this.studentService = studentService;
	}

	@PostMapping
	public StudentClassAssignmentResponse assign(@PathVariable long studentId,
			@Valid @RequestBody AssignStudentClassRequest request,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		authorizationService.requireAdmin(user);
		return assignmentService.assign(studentId, request);
	}

	@PostMapping("/leave")
	public StudentClassAssignmentResponse leave(@PathVariable long studentId,
			@Valid @RequestBody LeaveStudentClassRequest request,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		authorizationService.requireAdmin(user);
		return assignmentService.leave(studentId, request);
	}

	@GetMapping
	public List<StudentClassAssignmentResponse> history(@PathVariable long studentId,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		StudentResponse student = studentService.getById(studentId);
		authorizationService.requireCanRead(user, student);
		return assignmentService.history(studentId);
	}
}
