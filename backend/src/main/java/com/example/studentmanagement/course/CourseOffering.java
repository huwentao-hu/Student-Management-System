package com.example.studentmanagement.course;

import java.time.Instant;

import com.example.studentmanagement.auth.UserAccount;
import com.example.studentmanagement.schoolclass.SchoolClass;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "course_offerings", uniqueConstraints = @UniqueConstraint(columnNames = {
		"class_id", "academic_year", "semester", "course_id"
}))
public class CourseOffering {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "class_id", nullable = false)
	private SchoolClass schoolClass;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "teacher_id", nullable = false)
	private UserAccount teacher;

	@Column(nullable = false)
	private int academicYear;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private Semester semester;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected CourseOffering() {
	}

	public CourseOffering(Course course, SchoolClass schoolClass, UserAccount teacher, int academicYear,
			Semester semester) {
		this.course = course;
		this.schoolClass = schoolClass;
		this.teacher = teacher;
		this.academicYear = academicYear;
		this.semester = semester;
		this.createdAt = Instant.now();
		this.updatedAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public Course getCourse() {
		return course;
	}

	public SchoolClass getSchoolClass() {
		return schoolClass;
	}

	public UserAccount getTeacher() {
		return teacher;
	}

	public int getAcademicYear() {
		return academicYear;
	}

	public Semester getSemester() {
		return semester;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
