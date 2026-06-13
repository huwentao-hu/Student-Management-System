package com.example.studentmanagement.course;

import java.time.Instant;

public record CourseOfferingResponse(
		Long id,
		Long courseId,
		String courseCode,
		String courseName,
		Long classId,
		String className,
		Long teacherId,
		String teacherUsername,
		int academicYear,
		Semester semester,
		Instant createdAt,
		Instant updatedAt) {

	static CourseOfferingResponse from(CourseOffering offering) {
		return new CourseOfferingResponse(offering.getId(), offering.getCourse().getId(),
				offering.getCourse().getCourseCode(), offering.getCourse().getName(), offering.getSchoolClass().getId(),
				offering.getSchoolClass().getName(), offering.getTeacher().getId(), offering.getTeacher().getUsername(),
				offering.getAcademicYear(), offering.getSemester(), offering.getCreatedAt(), offering.getUpdatedAt());
	}
}
