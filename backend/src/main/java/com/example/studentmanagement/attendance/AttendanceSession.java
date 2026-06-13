package com.example.studentmanagement.attendance;

import java.time.Instant;
import java.time.LocalDate;

import com.example.studentmanagement.course.CourseOffering;

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
@Table(name = "attendance_sessions",
		uniqueConstraints = @UniqueConstraint(columnNames = { "course_offering_id", "session_date" }))
public class AttendanceSession {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "course_offering_id", nullable = false)
	private CourseOffering courseOffering;

	@Column(nullable = false)
	private LocalDate sessionDate;

	@Column(length = 200)
	private String topic;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected AttendanceSession() {
	}

	public AttendanceSession(CourseOffering courseOffering, LocalDate sessionDate, String topic) {
		this.courseOffering = courseOffering;
		this.sessionDate = sessionDate;
		this.topic = topic;
		this.createdAt = Instant.now();
		this.updatedAt = createdAt;
	}

	public Long getId() { return id; }
	public CourseOffering getCourseOffering() { return courseOffering; }
	public LocalDate getSessionDate() { return sessionDate; }
	public String getTopic() { return topic; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
