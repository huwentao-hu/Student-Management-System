package com.example.studentmanagement.course;

public class DuplicateCourseException extends RuntimeException {

	public DuplicateCourseException(String name) {
		super("Course already exists: " + name);
	}
}
