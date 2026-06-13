package com.example.studentmanagement.auth;

public class InvalidAccountRequestException extends RuntimeException {

	public InvalidAccountRequestException(String message) {
		super(message);
	}
}
