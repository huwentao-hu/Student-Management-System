package com.example.studentmanagement.student;

import java.time.Instant;
import java.time.LocalDate;

public record StudentResponse(
		Long id,
		String studentNumber,
		String name,
		String gender,
		LocalDate dateOfBirth,
		String phone,
		String email,
		LocalDate enrollmentDate,
		StudentStatus status,
		Instant createdAt,
		Instant updatedAt) {

	static StudentResponse from(Student student) {
		return new StudentResponse(student.getId(), student.getStudentNumber(), student.getName(), student.getGender(),
				student.getDateOfBirth(), student.getPhone(), student.getEmail(), student.getEnrollmentDate(),
				student.getStatus(), student.getCreatedAt(), student.getUpdatedAt());
	}
}
