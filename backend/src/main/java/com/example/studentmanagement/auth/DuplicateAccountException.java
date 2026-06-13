package com.example.studentmanagement.auth;

public class DuplicateAccountException extends RuntimeException {

	public DuplicateAccountException(String message) {
		super(message);
	}
}
