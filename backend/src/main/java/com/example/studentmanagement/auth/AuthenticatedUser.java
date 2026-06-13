package com.example.studentmanagement.auth;

public record AuthenticatedUser(Long userId, String username, UserRole role, Long studentId) {
}
