package com.example.studentmanagement.auth;

import java.time.Instant;

import com.example.studentmanagement.student.Student;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_accounts")
public class UserAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 64)
	private String username;

	@Column(nullable = false, length = 100)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private UserRole role;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "student_id", unique = true)
	private Student student;

	@Column(nullable = false)
	private boolean enabled;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected UserAccount() {
	}

	public UserAccount(String username, String passwordHash, UserRole role, Student student) {
		this.username = username;
		this.passwordHash = passwordHash;
		this.role = role;
		this.student = student;
		this.enabled = true;
		this.createdAt = Instant.now();
		this.updatedAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public UserRole getRole() {
		return role;
	}

	public Student getStudent() {
		return student;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
