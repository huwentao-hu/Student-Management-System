package com.example.studentmanagement.auth;

import java.time.Instant;

public record AccountResponse(
		Long id,
		String username,
		UserRole role,
		Long studentId,
		boolean enabled,
		Instant createdAt,
		Instant updatedAt) {

	static AccountResponse from(UserAccount account) {
		Long studentId = account.getStudent() == null ? null : account.getStudent().getId();
		return new AccountResponse(account.getId(), account.getUsername(), account.getRole(), studentId,
				account.isEnabled(), account.getCreatedAt(), account.getUpdatedAt());
	}
}
