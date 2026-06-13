package com.example.studentmanagement.course;

import java.util.List;

public record StudentTimetableResponse(
		Long studentId,
		int academicYear,
		Semester semester,
		List<Long> classIds,
		List<CourseOfferingResponse> offerings) {
}
