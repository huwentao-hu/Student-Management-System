package com.example.studentmanagement.course;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "courses")
public class Course {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 16)
	private String courseCode;

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Column(nullable = false, precision = 3, scale = 1)
	private BigDecimal credits;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private CourseStatus status;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected Course() {
	}

	public Course(String courseCode, String name, BigDecimal credits, CourseStatus status) {
		this.courseCode = courseCode;
		this.name = name;
		this.credits = credits;
		this.status = status;
		this.createdAt = Instant.now();
		this.updatedAt = createdAt;
	}

	public void assignGeneratedCourseCode(String courseCode) {
		this.courseCode = courseCode;
		this.updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public String getCourseCode() {
		return courseCode;
	}

	public String getName() {
		return name;
	}

	public BigDecimal getCredits() {
		return credits;
	}

	public CourseStatus getStatus() {
		return status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
