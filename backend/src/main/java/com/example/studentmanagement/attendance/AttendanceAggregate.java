package com.example.studentmanagement.attendance;

public interface AttendanceAggregate {

	Long getRecordedCount();

	Long getPresentCount();

	Long getLateCount();

	Long getExcusedCount();

	Long getAbsentCount();
}
