package com.example.studentmanagement.auth;

import java.time.Instant;

public record LoginResponse(
		String token,
		Instant expiresAt,
		Long userId,
		String username,
		UserRole role,
		Long studentId) {
}
