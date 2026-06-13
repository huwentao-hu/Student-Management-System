package com.example.studentmanagement.auth;

public class AccessDeniedException extends RuntimeException {

	public AccessDeniedException(String message) {
		super(message);
	}
}
