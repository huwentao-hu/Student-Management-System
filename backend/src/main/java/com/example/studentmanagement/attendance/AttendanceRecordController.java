package com.example.studentmanagement.attendance;

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

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@Validated
@RequestMapping("/api/attendance-records")
public class AttendanceRecordController {
	private final AttendanceService attendanceService;

	public AttendanceRecordController(AttendanceService attendanceService) {
		this.attendanceService = attendanceService;
	}

	@PutMapping
	public ResponseEntity<AttendanceRecordResponse> upsert(@Valid @RequestBody UpsertAttendanceRecordRequest request,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user,
			UriComponentsBuilder uriBuilder) {
		AttendanceUpsertResult result = attendanceService.upsertRecord(request, user);
		if (!result.created()) {
			return ResponseEntity.ok(result.record());
		}
		URI location = uriBuilder.path("/api/attendance-records/{id}").build(result.record().id());
		return ResponseEntity.created(location).body(result.record());
	}

	@GetMapping("/{id}")
	public AttendanceRecordResponse get(@PathVariable long id,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		return attendanceService.getRecord(id, user);
	}

	@GetMapping
	public PageResponse<AttendanceRecordResponse> search(@RequestParam(required = false) Long studentId,
			@RequestParam(required = false) Long courseOfferingId,
			@RequestParam(required = false) Long attendanceSessionId,
			@RequestParam(required = false) AttendanceStatus status,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		return attendanceService.searchRecords(studentId, courseOfferingId, attendanceSessionId, status, page, size, user);
	}
}
