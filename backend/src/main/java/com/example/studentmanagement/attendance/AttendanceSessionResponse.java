package com.example.studentmanagement.attendance;

import java.time.Instant;
import java.time.LocalDate;

import com.example.studentmanagement.course.Semester;

public record AttendanceSessionResponse(
		Long id, Long courseOfferingId, String courseCode, String courseName, Long classId, String className,
		Long teacherId, String teacherUsername, int academicYear, Semester semester, LocalDate sessionDate,
		String topic, Instant createdAt, Instant updatedAt) {

	static AttendanceSessionResponse from(AttendanceSession session) {
		var offering = session.getCourseOffering();
		return new AttendanceSessionResponse(session.getId(), offering.getId(), offering.getCourse().getCourseCode(),
				offering.getCourse().getName(), offering.getSchoolClass().getId(), offering.getSchoolClass().getName(),
				offering.getTeacher().getId(), offering.getTeacher().getUsername(), offering.getAcademicYear(),
				offering.getSemester(), session.getSessionDate(), session.getTopic(), session.getCreatedAt(),
				session.getUpdatedAt());
	}
}
