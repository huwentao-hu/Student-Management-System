package com.example.studentmanagement.grade;

import java.math.BigDecimal;

public interface StudentTermGradeAggregate {

	Long getGradeCount();

	BigDecimal getTotalCredits();

	BigDecimal getWeightedScoreTotal();

	Long getPassedCourseCount();
}
