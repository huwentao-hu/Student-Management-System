package com.example.studentmanagement.attendance;

import java.net.URI;
import java.time.LocalDate;

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

import com.example.studentmanagement.auth.AuthenticatedUser;
import com.example.studentmanagement.auth.AuthenticationInterceptor;
import com.example.studentmanagement.common.PageResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@Validated
@RequestMapping("/api/attendance-sessions")
public class AttendanceSessionController {
	private final AttendanceService attendanceService;

	public AttendanceSessionController(AttendanceService attendanceService) {
		this.attendanceService = attendanceService;
	}

	@PostMapping
	public ResponseEntity<AttendanceSessionResponse> create(@Valid @RequestBody CreateAttendanceSessionRequest request,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user,
			UriComponentsBuilder uriBuilder) {
		AttendanceSessionResponse response = attendanceService.createSession(request, user);
		URI location = uriBuilder.path("/api/attendance-sessions/{id}").build(response.id());
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping("/{id}")
	public AttendanceSessionResponse get(@PathVariable long id,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		return attendanceService.getSession(id, user);
	}

	@GetMapping
	public PageResponse<AttendanceSessionResponse> search(@RequestParam(required = false) Long courseOfferingId,
			@RequestParam(required = false) LocalDate sessionDate,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		return attendanceService.searchSessions(courseOfferingId, sessionDate, page, size, user);
	}
}
