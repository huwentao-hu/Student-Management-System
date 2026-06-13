package com.example.studentmanagement.grade;

import java.math.BigDecimal;
import java.time.Instant;

import com.example.studentmanagement.course.Semester;

public record GradeResponse(
		Long id,
		Long studentId,
		String studentNumber,
		String studentName,
		Long courseOfferingId,
		String courseCode,
		String courseName,
		Long classId,
		String className,
		Long teacherId,
		String teacherUsername,
		int academicYear,
		Semester semester,
		BigDecimal score,
		Instant createdAt,
		Instant updatedAt) {

	static GradeResponse from(Grade grade) {
		var offering = grade.getCourseOffering();
		return new GradeResponse(grade.getId(), grade.getStudent().getId(), grade.getStudent().getStudentNumber(),
				grade.getStudent().getName(), offering.getId(), offering.getCourse().getCourseCode(),
				offering.getCourse().getName(), offering.getSchoolClass().getId(), offering.getSchoolClass().getName(),
				offering.getTeacher().getId(), offering.getTeacher().getUsername(), offering.getAcademicYear(),
				offering.getSemester(), grade.getScore(), grade.getCreatedAt(), grade.getUpdatedAt());
	}
}
