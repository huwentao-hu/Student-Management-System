package com.example.studentmanagement.schoolclass;

import java.time.Instant;

public record SchoolClassResponse(
		Long id,
		String name,
		int entryYear,
		Long homeroomTeacherId,
		String homeroomTeacherUsername,
		Instant createdAt,
		Instant updatedAt) {

	static SchoolClassResponse from(SchoolClass schoolClass) {
		return new SchoolClassResponse(schoolClass.getId(), schoolClass.getName(), schoolClass.getEntryYear(),
				schoolClass.getHomeroomTeacher().getId(), schoolClass.getHomeroomTeacher().getUsername(),
				schoolClass.getCreatedAt(), schoolClass.getUpdatedAt());
	}
}
