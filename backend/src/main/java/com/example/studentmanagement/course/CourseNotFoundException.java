package com.example.studentmanagement.course;

public class CourseNotFoundException extends RuntimeException {

	public CourseNotFoundException(long id) {
		super("Course not found: " + id);
	}
}
