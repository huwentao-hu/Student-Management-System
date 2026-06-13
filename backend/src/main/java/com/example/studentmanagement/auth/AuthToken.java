package com.example.studentmanagement.auth;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "auth_tokens")
public class AuthToken {

	@Id
	@Column(length = 64)
	private String tokenHash;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_account_id", nullable = false)
	private UserAccount userAccount;

	@Column(nullable = false)
	private Instant expiresAt;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected AuthToken() {
	}

	public AuthToken(String tokenHash, UserAccount userAccount, Instant expiresAt) {
		this.tokenHash = tokenHash;
		this.userAccount = userAccount;
		this.expiresAt = expiresAt;
		this.createdAt = Instant.now();
	}

	public UserAccount getUserAccount() {
		return userAccount;
	}

	public Instant getExpiresAt() {
		return expiresAt;
	}
}
