package com.example.studentmanagement.course;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateCourseOfferingRequest(
		@NotNull Long courseId,
		@NotNull Long classId,
		@NotNull Long teacherId,
		@Min(1900) @Max(2200) int academicYear,
		@NotNull Semester semester) {
}
