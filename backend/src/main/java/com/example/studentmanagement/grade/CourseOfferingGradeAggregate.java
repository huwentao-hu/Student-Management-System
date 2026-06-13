package com.example.studentmanagement.grade;

import java.math.BigDecimal;

public interface CourseOfferingGradeAggregate {

	Long getGradedCount();

	Double getAverageScore();

	BigDecimal getHighestScore();

	BigDecimal getLowestScore();

	Long getPassedCount();
}
