package com.example.studentmanagement.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BootstrapAdminInitializer implements ApplicationRunner {

	private final UserAccountRepository userAccountRepository;
	private final PasswordEncoder passwordEncoder;
	private final String username;
	private final String password;

	public BootstrapAdminInitializer(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder,
			@Value("${app.bootstrap-admin.username}") String username,
			@Value("${app.bootstrap-admin.password}") String password) {
		this.userAccountRepository = userAccountRepository;
		this.passwordEncoder = passwordEncoder;
		this.username = username;
		this.password = password;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (!password.isBlank() && !userAccountRepository.existsByUsername(username)) {
			userAccountRepository.save(new UserAccount(username, passwordEncoder.encode(password), UserRole.ADMIN, null));
		}
	}
}
