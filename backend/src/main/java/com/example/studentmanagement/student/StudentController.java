package com.example.studentmanagement.student;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.studentmanagement.auth.AuthenticatedUser;
import com.example.studentmanagement.auth.AuthenticationInterceptor;
import com.example.studentmanagement.common.PageResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Validated
@RestController
@RequestMapping("/api/students")
public class StudentController {

	private final StudentService studentService;
	private final StudentAuthorizationService authorizationService;

	public StudentController(StudentService studentService, StudentAuthorizationService authorizationService) {
		this.studentService = studentService;
		this.authorizationService = authorizationService;
	}

	@PostMapping
	public ResponseEntity<StudentResponse> create(@Valid @RequestBody CreateStudentRequest request,
			UriComponentsBuilder uriBuilder,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		authorizationService.requireAdmin(user);
		StudentResponse response = studentService.create(request);
		URI location = uriBuilder.path("/api/students/{id}").build(response.id());
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping("/{id}")
	public StudentResponse getById(@PathVariable long id,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		StudentResponse response = studentService.getById(id);
		authorizationService.requireCanRead(user, response);
		return response;
	}

	@PutMapping("/{id}")
	public StudentResponse update(@PathVariable long id, @Valid @RequestBody UpdateStudentRequest request,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		authorizationService.requireAdmin(user);
		return studentService.update(id, request);
	}

	@GetMapping(params = "studentNumber")
	public StudentResponse getByStudentNumber(@RequestParam @NotBlank String studentNumber,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		StudentResponse response = studentService.getByStudentNumber(studentNumber);
		authorizationService.requireCanRead(user, response);
		return response;
	}

	@GetMapping(params = "!studentNumber")
	public PageResponse<StudentResponse> search(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) StudentStatus status,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		authorizationService.requireCanList(user);
		return studentService.search(keyword, status, page, size);
	}
}
