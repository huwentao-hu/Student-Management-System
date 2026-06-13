package com.example.studentmanagement.common;

import java.time.Instant;

public record HealthResponse(String status, String database, Instant checkedAt) {
}
