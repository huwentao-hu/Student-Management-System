package com.example.studentmanagement.grade;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.studentmanagement.course.Semester;

public interface GradeRepository extends JpaRepository<Grade, Long>, JpaSpecificationExecutor<Grade> {

	Optional<Grade> findByStudentIdAndCourseOfferingId(long studentId, long courseOfferingId);

	@Query("""
			select count(grade) as gradedCount,
				avg(grade.score) as averageScore,
				max(grade.score) as highestScore,
				min(grade.score) as lowestScore,
				sum(case when grade.score >= 60.0 then 1 else 0 end) as passedCount
			from Grade grade
			where grade.courseOffering.id = :courseOfferingId
			""")
	CourseOfferingGradeAggregate aggregateByCourseOfferingId(@Param("courseOfferingId") long courseOfferingId);

	@Query("""
			select count(grade) as gradeCount,
				sum(grade.courseOffering.course.credits) as totalCredits,
				sum(grade.score * grade.courseOffering.course.credits) as weightedScoreTotal,
				sum(case when grade.score >= 60.0 then 1 else 0 end) as passedCourseCount
			from Grade grade
			where grade.student.id = :studentId
				and grade.courseOffering.academicYear = :academicYear
				and grade.courseOffering.semester = :semester
			""")
	StudentTermGradeAggregate aggregateByStudentTerm(@Param("studentId") long studentId,
			@Param("academicYear") int academicYear, @Param("semester") Semester semester);
}
