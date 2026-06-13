package com.example.studentmanagement.attendance;

public class DuplicateAttendanceSessionException extends RuntimeException {
	public DuplicateAttendanceSessionException() {
		super("Attendance session already exists for this course offering and date");
	}
}
