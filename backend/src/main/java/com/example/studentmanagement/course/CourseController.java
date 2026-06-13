package com.example.studentmanagement.course;

import java.net.URI;

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

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@Validated
@RequestMapping("/api/courses")
public class CourseController {

	private final CourseService courseService;

	public CourseController(CourseService courseService) {
		this.courseService = courseService;
	}

	@PostMapping
	public ResponseEntity<CourseResponse> create(@Valid @RequestBody CreateCourseRequest request,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user,
			UriComponentsBuilder uriBuilder) {
		requireAdmin(user);
		CourseResponse response = courseService.create(request);
		URI location = uriBuilder.path("/api/courses/{id}").build(response.id());
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping("/{id}")
	public CourseResponse getById(@PathVariable long id,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		requireStaff(user);
		return courseService.getById(id);
	}

	@GetMapping
	public PageResponse<CourseResponse> search(
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) CourseStatus status,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		requireStaff(user);
		return courseService.search(keyword, status, page, size);
	}

	private void requireAdmin(AuthenticatedUser user) {
		if (user.role() != UserRole.ADMIN) {
			throw new AccessDeniedException("Administrator role is required");
		}
	}

	private void requireStaff(AuthenticatedUser user) {
		if (user.role() == UserRole.STUDENT) {
			throw new AccessDeniedException("Students cannot access course management");
		}
	}
}
