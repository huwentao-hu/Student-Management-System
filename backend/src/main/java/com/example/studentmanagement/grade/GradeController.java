package com.example.studentmanagement.grade;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.studentmanagement.auth.AuthenticatedUser;
import com.example.studentmanagement.auth.AuthenticationInterceptor;
import com.example.studentmanagement.common.PageResponse;
import com.example.studentmanagement.course.Semester;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@Validated
@RequestMapping("/api/grades")
public class GradeController {

	private final GradeService gradeService;

	public GradeController(GradeService gradeService) {
		this.gradeService = gradeService;
	}

	@PutMapping
	public ResponseEntity<GradeResponse> upsert(@Valid @RequestBody UpsertGradeRequest request,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user,
			UriComponentsBuilder uriBuilder) {
		GradeUpsertResult result = gradeService.upsert(request, user);
		if (!result.created()) {
			return ResponseEntity.ok(result.grade());
		}
		URI location = uriBuilder.path("/api/grades/{id}").build(result.grade().id());
		return ResponseEntity.created(location).body(result.grade());
	}

	@GetMapping("/{id}")
	public GradeResponse getById(@PathVariable long id,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		return gradeService.getById(id, user);
	}

	@GetMapping
	public PageResponse<GradeResponse> search(
			@RequestParam(required = false) Long studentId,
			@RequestParam(required = false) Long courseOfferingId,
			@RequestParam(required = false) @Min(1900) @Max(2200) Integer academicYear,
			@RequestParam(required = false) Semester semester,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		return gradeService.search(studentId, courseOfferingId, academicYear, semester, page, size, user);
	}
}
