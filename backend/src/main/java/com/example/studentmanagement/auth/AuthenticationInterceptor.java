package com.example.studentmanagement.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

	public static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";

	private final AuthService authService;

	public AuthenticationInterceptor(AuthService authService) {
		this.authService = authService;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (CorsUtils.isPreFlightRequest(request)) {
			return true;
		}
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization == null || !authorization.startsWith("Bearer ") || authorization.length() == 7) {
			throw new UnauthorizedException("Bearer authentication token is required");
		}
		String rawToken = authorization.substring(7).trim();
		request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, authService.authenticate(rawToken));
		return true;
	}
}
