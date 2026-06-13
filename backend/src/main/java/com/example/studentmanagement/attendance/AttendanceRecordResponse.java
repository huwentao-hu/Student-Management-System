package com.example.studentmanagement.attendance;

import java.time.Instant;
import java.time.LocalDate;

public record AttendanceRecordResponse(
		Long id, Long attendanceSessionId, LocalDate sessionDate, Long studentId, String studentNumber,
		String studentName, Long courseOfferingId, String courseCode, String courseName, Long classId,
		String className, Long teacherId, String teacherUsername, AttendanceStatus status, Instant createdAt,
		Instant updatedAt) {

	static AttendanceRecordResponse from(AttendanceRecord record) {
		var session = record.getAttendanceSession();
		var offering = session.getCourseOffering();
		return new AttendanceRecordResponse(record.getId(), session.getId(), session.getSessionDate(),
				record.getStudent().getId(), record.getStudent().getStudentNumber(), record.getStudent().getName(),
				offering.getId(), offering.getCourse().getCourseCode(), offering.getCourse().getName(),
				offering.getSchoolClass().getId(), offering.getSchoolClass().getName(), offering.getTeacher().getId(),
				offering.getTeacher().getUsername(), record.getStatus(), record.getCreatedAt(), record.getUpdatedAt());
	}
}
