package com.example.studentmanagement.course;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCourseRequest(
		@NotBlank @Size(max = 100) String name,
		@NotNull @DecimalMin("0.5") @DecimalMax("30.0") @Digits(integer = 2, fraction = 1) BigDecimal credits,
		CourseStatus status) {
}
