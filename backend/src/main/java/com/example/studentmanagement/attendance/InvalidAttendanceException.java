package com.example.studentmanagement.attendance;

public class InvalidAttendanceException extends RuntimeException {
	public InvalidAttendanceException(String message) {
		super(message);
	}
}
