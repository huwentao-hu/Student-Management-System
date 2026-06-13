package com.example.studentmanagement.course;

public class DuplicateCourseOfferingException extends RuntimeException {

	public DuplicateCourseOfferingException() {
		super("Course is already offered to this class in the selected academic year and semester");
	}
}
