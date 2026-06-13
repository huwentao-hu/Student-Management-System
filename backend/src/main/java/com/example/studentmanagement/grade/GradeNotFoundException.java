package com.example.studentmanagement.grade;

public class GradeNotFoundException extends RuntimeException {

	public GradeNotFoundException(long id) {
		super("Grade not found: " + id);
	}
}
