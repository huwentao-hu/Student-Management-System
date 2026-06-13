package com.example.studentmanagement.schoolclass;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record LeaveStudentClassRequest(@NotNull LocalDate effectiveDate) {
}
