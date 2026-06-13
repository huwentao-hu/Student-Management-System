package com.example.studentmanagement.schoolclass;

import java.time.Instant;

import com.example.studentmanagement.auth.UserAccount;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "school_classes", uniqueConstraints = @UniqueConstraint(columnNames = { "entry_year", "name" }))
public class SchoolClass {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false)
	private int entryYear;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "homeroom_teacher_id", nullable = false)
	private UserAccount homeroomTeacher;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected SchoolClass() {
	}

	public SchoolClass(String name, int entryYear, UserAccount homeroomTeacher) {
		this.name = name;
		this.entryYear = entryYear;
		this.homeroomTeacher = homeroomTeacher;
		this.createdAt = Instant.now();
		this.updatedAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getEntryYear() {
		return entryYear;
	}

	public UserAccount getHomeroomTeacher() {
		return homeroomTeacher;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
