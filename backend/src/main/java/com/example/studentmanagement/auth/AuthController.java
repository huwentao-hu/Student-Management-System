package com.example.studentmanagement.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
		authService.logout(authorization.substring(7).trim());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/tokens/cleanup")
	public TokenCleanupResponse cleanupExpiredTokens(
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		if (user.role() != UserRole.ADMIN) {
			throw new AccessDeniedException("Administrator role is required");
		}
		return new TokenCleanupResponse(authService.cleanupExpiredTokens());
	}
}
