package com.example.studentmanagement.attendance;

import jakarta.validation.constraints.NotNull;

public record UpsertAttendanceRecordRequest(
		@NotNull Long attendanceSessionId,
		@NotNull Long studentId,
		@NotNull AttendanceStatus status) {
}
