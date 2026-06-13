package com.example.studentmanagement.student;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record UpdateStudentRequest(
		@NotBlank @Size(max = 100) String name,
		@Size(max = 16) String gender,
		@Past LocalDate dateOfBirth,
		@Size(max = 32) String phone,
		@Email @Size(max = 255) String email,
		LocalDate enrollmentDate,
		@NotNull StudentStatus status) {
}
