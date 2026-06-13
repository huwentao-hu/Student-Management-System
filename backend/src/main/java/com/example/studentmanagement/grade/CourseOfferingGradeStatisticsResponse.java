package com.example.studentmanagement.grade;

import java.math.BigDecimal;

import com.example.studentmanagement.course.CourseOffering;
import com.example.studentmanagement.course.Semester;

public record CourseOfferingGradeStatisticsResponse(
		Long courseOfferingId,
		String courseCode,
		String courseName,
		Long classId,
		String className,
		Long teacherId,
		String teacherUsername,
		int academicYear,
		Semester semester,
		long gradedCount,
		BigDecimal averageScore,
		BigDecimal highestScore,
		BigDecimal lowestScore,
		long passedCount,
		long failedCount,
		BigDecimal passRate) {

	static CourseOfferingGradeStatisticsResponse from(CourseOffering offering, long gradedCount,
			BigDecimal averageScore, BigDecimal highestScore, BigDecimal lowestScore, long passedCount,
			long failedCount, BigDecimal passRate) {
		return new CourseOfferingGradeStatisticsResponse(offering.getId(), offering.getCourse().getCourseCode(),
				offering.getCourse().getName(), offering.getSchoolClass().getId(), offering.getSchoolClass().getName(),
				offering.getTeacher().getId(), offering.getTeacher().getUsername(), offering.getAcademicYear(),
				offering.getSemester(), gradedCount, averageScore, highestScore, lowestScore, passedCount, failedCount,
				passRate);
	}
}
