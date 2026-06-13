package com.example.studentmanagement.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

	private final UserAccountRepository userAccountRepository;
	private final AuthTokenRepository authTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final SecureRandom secureRandom = new SecureRandom();
	private final Duration tokenTtl;

	public AuthService(UserAccountRepository userAccountRepository, AuthTokenRepository authTokenRepository,
			PasswordEncoder passwordEncoder, @Value("${app.auth.token-ttl-hours}") long tokenTtlHours) {
		this.userAccountRepository = userAccountRepository;
		this.authTokenRepository = authTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenTtl = Duration.ofHours(tokenTtlHours);
	}

	public LoginResponse login(LoginRequest request) {
		UserAccount account = userAccountRepository.findByUsername(request.username().trim())
			.filter(UserAccount::isEnabled)
			.filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
			.orElseThrow(InvalidCredentialsException::new);

		String rawToken = generateToken();
		Instant expiresAt = Instant.now().plus(tokenTtl);
		authTokenRepository.save(new AuthToken(hashToken(rawToken), account, expiresAt));
		Long studentId = account.getStudent() == null ? null : account.getStudent().getId();
		return new LoginResponse(rawToken, expiresAt, account.getId(), account.getUsername(), account.getRole(), studentId);
	}

	@Transactional(readOnly = true)
	public AuthenticatedUser authenticate(String rawToken) {
		UserAccount account = authTokenRepository.findValidToken(hashToken(rawToken), Instant.now())
			.map(AuthToken::getUserAccount)
			.filter(UserAccount::isEnabled)
			.orElseThrow(() -> new UnauthorizedException("Invalid or expired authentication token"));
		Long studentId = account.getStudent() == null ? null : account.getStudent().getId();
		return new AuthenticatedUser(account.getId(), account.getUsername(), account.getRole(), studentId);
	}

	public void logout(String rawToken) {
		authTokenRepository.deleteById(hashToken(rawToken));
	}

	public int cleanupExpiredTokens() {
		return authTokenRepository.deleteExpiredTokens(Instant.now());
	}

	private String generateToken() {
		byte[] tokenBytes = new byte[32];
		secureRandom.nextBytes(tokenBytes);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
	}

	private String hashToken(String rawToken) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		}
		catch (NoSuchAlgorithmException exception) {
			throw new IllegalStateException("SHA-256 is not available", exception);
		}
	}
}
