package com.example.studentmanagement.attendance;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAttendanceSessionRequest(
		@NotNull Long courseOfferingId,
		@NotNull LocalDate sessionDate,
		@Size(max = 200) String topic) {
}
