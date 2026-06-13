package com.example.studentmanagement.grade;

import java.math.BigDecimal;

import com.example.studentmanagement.course.Semester;
import com.example.studentmanagement.student.Student;

public record StudentTermGradeStatisticsResponse(
		Long studentId,
		String studentNumber,
		String studentName,
		int academicYear,
		Semester semester,
		long gradeCount,
		BigDecimal totalCredits,
		BigDecimal weightedAverageScore,
		long passedCourseCount,
		long failedCourseCount) {

	static StudentTermGradeStatisticsResponse from(Student student, int academicYear, Semester semester, long gradeCount,
			BigDecimal totalCredits, BigDecimal weightedAverageScore, long passedCourseCount, long failedCourseCount) {
		return new StudentTermGradeStatisticsResponse(student.getId(), student.getStudentNumber(), student.getName(),
				academicYear, semester, gradeCount, totalCredits, weightedAverageScore, passedCourseCount,
				failedCourseCount);
	}
}
