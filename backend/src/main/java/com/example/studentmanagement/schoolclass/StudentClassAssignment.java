package com.example.studentmanagement.schoolclass;

import java.time.Instant;
import java.time.LocalDate;

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

@Entity
@Table(name = "student_class_assignments")
public class StudentClassAssignment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	private Student student;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "school_class_id", nullable = false)
	private SchoolClass schoolClass;

	@Column(nullable = false)
	private LocalDate startDate;

	private LocalDate endDate;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	protected StudentClassAssignment() {
	}

	public StudentClassAssignment(Student student, SchoolClass schoolClass, LocalDate startDate) {
		this.student = student;
		this.schoolClass = schoolClass;
		this.startDate = startDate;
		this.createdAt = Instant.now();
	}

	public void close(LocalDate endDate) {
		this.endDate = endDate;
	}

	public Long getId() {
		return id;
	}

	public Student getStudent() {
		return student;
	}

	public SchoolClass getSchoolClass() {
		return schoolClass;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
