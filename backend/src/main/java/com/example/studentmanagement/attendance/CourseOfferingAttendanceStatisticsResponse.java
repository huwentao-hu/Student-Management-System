package com.example.studentmanagement.attendance;

import java.math.BigDecimal;

import com.example.studentmanagement.course.CourseOffering;
import com.example.studentmanagement.course.Semester;

public record CourseOfferingAttendanceStatisticsResponse(
		Long courseOfferingId,
		String courseCode,
		String courseName,
		Long classId,
		String className,
		Long teacherId,
		String teacherUsername,
		int academicYear,
		Semester semester,
		long sessionCount,
		long recordedCount,
		long presentCount,
		long lateCount,
		long excusedCount,
		long absentCount,
		BigDecimal attendanceRate) {

	static CourseOfferingAttendanceStatisticsResponse from(CourseOffering offering, long sessionCount,
			AttendanceStatisticsSummary summary) {
		return new CourseOfferingAttendanceStatisticsResponse(offering.getId(), offering.getCourse().getCourseCode(),
				offering.getCourse().getName(), offering.getSchoolClass().getId(), offering.getSchoolClass().getName(),
				offering.getTeacher().getId(), offering.getTeacher().getUsername(), offering.getAcademicYear(),
				offering.getSemester(), sessionCount, summary.recordedCount(), summary.presentCount(), summary.lateCount(),
				summary.excusedCount(), summary.absentCount(), summary.attendanceRate());
	}
}
