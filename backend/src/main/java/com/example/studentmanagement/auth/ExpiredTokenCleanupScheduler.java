package com.example.studentmanagement.auth;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ExpiredTokenCleanupScheduler {

	private final AuthService authService;

	public ExpiredTokenCleanupScheduler(AuthService authService) {
		this.authService = authService;
	}

	@Scheduled(fixedDelayString = "${app.auth.token-cleanup-interval-ms}",
			initialDelayString = "${app.auth.token-cleanup-initial-delay-ms}")
	public void cleanupExpiredTokens() {
		authService.cleanupExpiredTokens();
	}
}
