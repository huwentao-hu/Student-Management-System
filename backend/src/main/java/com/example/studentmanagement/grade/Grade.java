package com.example.studentmanagement.grade;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.studentmanagement.course.CourseOffering;
import com.example.studentmanagement.student.Student;

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
@Table(name = "grades", uniqueConstraints = @UniqueConstraint(columnNames = { "student_id", "course_offering_id" }))
public class Grade {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	private Student student;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_offering_id", nullable = false)
	private CourseOffering courseOffering;

	@Column(nullable = false, precision = 4, scale = 1)
	private BigDecimal score;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected Grade() {
	}

	public Grade(Student student, CourseOffering courseOffering, BigDecimal score) {
		this.student = student;
		this.courseOffering = courseOffering;
		this.score = score;
		this.createdAt = Instant.now();
		this.updatedAt = createdAt;
	}

	public void updateScore(BigDecimal score) {
		this.score = score;
		this.updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public Student getStudent() {
		return student;
	}

	public CourseOffering getCourseOffering() {
		return courseOffering;
	}

	public BigDecimal getScore() {
		return score;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
