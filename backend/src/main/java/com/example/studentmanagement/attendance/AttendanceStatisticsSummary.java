package com.example.studentmanagement.attendance;

import java.math.BigDecimal;

record AttendanceStatisticsSummary(
		long recordedCount,
		long presentCount,
		long lateCount,
		long excusedCount,
		long absentCount,
		BigDecimal attendanceRate) {
}
