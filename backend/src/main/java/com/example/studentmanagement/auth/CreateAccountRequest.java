package com.example.studentmanagement.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
		@NotBlank @Size(min = 3, max = 64) String username,
		@NotBlank @Size(min = 8, max = 128) String password,
		@NotNull UserRole role,
		Long studentId) {
}
