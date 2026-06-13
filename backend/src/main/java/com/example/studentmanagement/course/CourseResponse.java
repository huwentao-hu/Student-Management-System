package com.example.studentmanagement.course;

import java.math.BigDecimal;
import java.time.Instant;

public record CourseResponse(
		Long id,
		String courseCode,
		String name,
		BigDecimal credits,
		CourseStatus status,
		Instant createdAt,
		Instant updatedAt) {

	static CourseResponse from(Course course) {
		return new CourseResponse(course.getId(), course.getCourseCode(), course.getName(), course.getCredits(),
				course.getStatus(), course.getCreatedAt(), course.getUpdatedAt());
	}
}
