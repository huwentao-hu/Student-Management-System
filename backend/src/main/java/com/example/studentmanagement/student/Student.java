package com.example.studentmanagement.student;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "student_number", nullable = false, unique = true, length = 32)
	private String studentNumber;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(length = 16)
	private String gender;

	private LocalDate dateOfBirth;

	@Column(length = 32)
	private String phone;

	private String email;

	private LocalDate enrollmentDate;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private StudentStatus status;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected Student() {
	}

	public Student(String studentNumber, String name, String gender, LocalDate dateOfBirth, String phone, String email,
			LocalDate enrollmentDate) {
		this.studentNumber = studentNumber;
		this.name = name;
		this.gender = gender;
		this.dateOfBirth = dateOfBirth;
		this.phone = phone;
		this.email = email;
		this.enrollmentDate = enrollmentDate;
		this.status = StudentStatus.ACTIVE;
		this.createdAt = Instant.now();
		this.updatedAt = createdAt;
	}

	public void assignGeneratedStudentNumber(String studentNumber) {
		this.studentNumber = studentNumber;
		this.updatedAt = Instant.now();
	}

	public void update(String name, String gender, LocalDate dateOfBirth, String phone, String email,
			LocalDate enrollmentDate, StudentStatus status) {
		this.name = name;
		this.gender = gender;
		this.dateOfBirth = dateOfBirth;
		this.phone = phone;
		this.email = email;
		this.enrollmentDate = enrollmentDate;
		this.status = status;
		this.updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getStudentNumber() {
		return studentNumber;
	}

	public String getName() {
		return name;
	}

	public String getGender() {
		return gender;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public String getPhone() {
		return phone;
	}

	public String getEmail() {
		return email;
	}

	public LocalDate getEnrollmentDate() {
		return enrollmentDate;
	}

	public StudentStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
