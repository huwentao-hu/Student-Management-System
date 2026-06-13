package com.example.studentmanagement.attendance;

public class AttendanceSessionNotFoundException extends RuntimeException {
	public AttendanceSessionNotFoundException(long id) {
		super("Attendance session not found: " + id);
	}
}
