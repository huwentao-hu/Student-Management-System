package com.example.studentmanagement.course;

public class CourseOfferingNotFoundException extends RuntimeException {

	public CourseOfferingNotFoundException(long id) {
		super("Course offering not found: " + id);
	}
}
