package com.example.studentmanagement.schoolclass;

public class DuplicateSchoolClassException extends RuntimeException {

	public DuplicateSchoolClassException(int entryYear, String name) {
		super("Class already exists: " + entryYear + " / " + name);
	}
}
