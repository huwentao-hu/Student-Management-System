package com.example.studentmanagement.auth;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebAuthenticationConfiguration implements WebMvcConfigurer {

	private final AuthenticationInterceptor authenticationInterceptor;
	private final String[] allowedOrigins;

	public WebAuthenticationConfiguration(AuthenticationInterceptor authenticationInterceptor,
			@Value("${app.cors.allowed-origins}") String allowedOrigins) {
		this.authenticationInterceptor = authenticationInterceptor;
		this.allowedOrigins = Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(value -> !value.isEmpty())
			.toArray(String[]::new);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(authenticationInterceptor)
			.addPathPatterns("/api/auth/logout", "/api/auth/tokens/cleanup", "/api/students/**", "/api/accounts/**", "/api/classes/**", "/api/courses/**",
					"/api/course-offerings/**", "/api/grades/**", "/api/grade-statistics/**", "/api/attendance-sessions/**",
					"/api/attendance-records/**", "/api/attendance-statistics/**");
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
			.allowedOrigins(allowedOrigins)
			.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
			.allowedHeaders("Authorization", "Content-Type")
			.maxAge(3600);
	}
}
