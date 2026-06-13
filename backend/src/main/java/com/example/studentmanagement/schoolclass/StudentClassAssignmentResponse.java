package com.example.studentmanagement.schoolclass;

import java.time.Instant;
import java.time.LocalDate;

public record StudentClassAssignmentResponse(
		Long id,
		Long studentId,
		Long classId,
		String className,
		int classEntryYear,
		LocalDate startDate,
		LocalDate endDate,
		boolean current,
		Instant createdAt) {

	static StudentClassAssignmentResponse from(StudentClassAssignment assignment) {
		return new StudentClassAssignmentResponse(assignment.getId(), assignment.getStudent().getId(),
				assignment.getSchoolClass().getId(), assignment.getSchoolClass().getName(),
				assignment.getSchoolClass().getEntryYear(), assignment.getStartDate(), assignment.getEndDate(),
				assignment.getEndDate() == null, assignment.getCreatedAt());
	}
}
