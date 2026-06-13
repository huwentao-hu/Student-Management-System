package com.example.studentmanagement.attendance;

public class AttendanceRecordNotFoundException extends RuntimeException {
	public AttendanceRecordNotFoundException(long id) {
		super("Attendance record not found: " + id);
	}
}
