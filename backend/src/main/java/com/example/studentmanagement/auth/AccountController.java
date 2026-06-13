package com.example.studentmanagement.auth;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@PostMapping
	public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request,
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user,
			UriComponentsBuilder uriBuilder) {
		if (user.role() != UserRole.ADMIN) {
			throw new AccessDeniedException("Administrator role is required");
		}
		AccountResponse response = accountService.create(request);
		URI location = uriBuilder.path("/api/accounts/{id}").build(response.id());
		return ResponseEntity.created(location).body(response);
	}

	@GetMapping("/teachers")
	public List<AccountResponse> listTeachers(
			@RequestAttribute(AuthenticationInterceptor.AUTHENTICATED_USER_ATTRIBUTE) AuthenticatedUser user) {
		if (user.role() != UserRole.ADMIN) {
			throw new AccessDeniedException("Administrator role is required");
		}
		return accountService.listEnabledTeachers();
	}
}
