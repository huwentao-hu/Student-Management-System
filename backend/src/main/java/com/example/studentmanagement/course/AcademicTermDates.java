package com.example.studentmanagement.course;

import java.time.LocalDate;

public final class AcademicTermDates {

	private AcademicTermDates() {
	}

	public static LocalDate start(int academicYear, Semester semester) {
		return semester == Semester.FIRST ? LocalDate.of(academicYear, 9, 1) : LocalDate.of(academicYear + 1, 2, 1);
	}

	public static LocalDate end(int academicYear, Semester semester) {
		return semester == Semester.FIRST ? LocalDate.of(academicYear + 1, 1, 31)
				: LocalDate.of(academicYear + 1, 7, 31);
	}
}
