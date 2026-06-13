package com.example.studentmanagement.schoolclass;

public class SchoolClassNotFoundException extends RuntimeException {

	public SchoolClassNotFoundException(long id) {
		super("Class not found: " + id);
	}
}
