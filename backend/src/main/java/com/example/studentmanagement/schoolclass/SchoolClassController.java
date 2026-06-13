package com.example.studentmanagement.schoolclass;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.studentmanagement.auth.AccessDeniedException;
import com.example.studentmanagement.auth.AuthenticatedUser;
import com.example.studentmanagement.auth.AuthenticationInterceptor;
import com.example.studentmanagement.auth.UserRole;
import com.example.studentmanagement.common.PageResponse;
import com.example.studentmanagement.student.StudentResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@Validated
@RequestMapping("/api/classes")
public class SchoolClassController {

	private final SchoolClassService schoolClassService;
	private final StudentClassAssignmentService assignmentService;

	public SchoolClassController(SchoolClassService schoolClassService, StudentClassAssignmentService assignmentService) {
		this.schoolClassService = schoolClassService;
		this.assignmentService = assignmentService;
	}

	@PostMapping
	public ResponseEntity<SchoolClassResponse> create(@Valid @RequestBody CreateSchoolClassRequest request,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user,
			UriComponentsBuilder uriBuilder) {
		requireAdmin(user);
		SchoolClassResponse response = schoolClassService.create(request);
		URI location = uriBuilder.path("/api/classes/{id}").build(response.id());
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping("/{id}")
	public SchoolClassResponse getById(@PathVariable long id,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		requireStaff(user);
		return schoolClassService.getById(id);
	}

	@GetMapping("/{id}/students")
	public List<StudentResponse> currentStudents(@PathVariable long id,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		requireStaff(user);
		return assignmentService.currentStudents(id);
	}

	@GetMapping
	public PageResponse<SchoolClassResponse> search(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) @Min(1900) @Max(2200) Integer entryYear,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		requireStaff(user);
		return schoolClassService.search(keyword, entryYear, page, size);
	}

	private void requireAdmin(AuthenticatedUser user) {
		if (user.role() != UserRole.ADMIN) {
			throw new AccessDeniedException("Administrator role is required");
		}
	}

	private void requireStaff(AuthenticatedUser user) {
		if (user.role() == UserRole.STUDENT) {
			throw new AccessDeniedException("Students cannot access class management");
		}
	}
}
