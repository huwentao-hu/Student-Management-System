package com.example.studentmanagement.attendance;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "attendance_records",
		uniqueConstraints = @UniqueConstraint(columnNames = { "attendance_session_id", "student_id" }))
public class AttendanceRecord {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "attendance_session_id", nullable = false)
	private AttendanceSession attendanceSession;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "student_id", nullable = false)
	private Student student;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private AttendanceStatus status;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	@Column(nullable = false)
	private Instant updatedAt;

	protected AttendanceRecord() {
	}

	public AttendanceRecord(AttendanceSession attendanceSession, Student student, AttendanceStatus status) {
		this.attendanceSession = attendanceSession;
		this.student = student;
		this.status = status;
		this.createdAt = Instant.now();
		this.updatedAt = createdAt;
	}

	public void updateStatus(AttendanceStatus status) {
		this.status = status;
		this.updatedAt = Instant.now();
	}

	public Long getId() { return id; }
	public AttendanceSession getAttendanceSession() { return attendanceSession; }
	public Student getStudent() { return student; }
	public AttendanceStatus getStatus() { return status; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
}
