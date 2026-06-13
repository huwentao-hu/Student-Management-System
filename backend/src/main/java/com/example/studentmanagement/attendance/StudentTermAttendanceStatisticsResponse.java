package com.example.studentmanagement.attendance;

import java.math.BigDecimal;

import com.example.studentmanagement.course.Semester;
import com.example.studentmanagement.student.Student;

public record StudentTermAttendanceStatisticsResponse(
		Long studentId,
		String studentNumber,
		String studentName,
		int academicYear,
		Semester semester,
		long recordedCount,
		long presentCount,
		long lateCount,
		long excusedCount,
		long absentCount,
		BigDecimal attendanceRate) {

	static StudentTermAttendanceStatisticsResponse from(Student student, int academicYear, Semester semester,
			AttendanceStatisticsSummary summary) {
		return new StudentTermAttendanceStatisticsResponse(student.getId(), student.getStudentNumber(), student.getName(),
				academicYear, semester, summary.recordedCount(), summary.presentCount(), summary.lateCount(),
				summary.excusedCount(), summary.absentCount(), summary.attendanceRate());
	}
}
