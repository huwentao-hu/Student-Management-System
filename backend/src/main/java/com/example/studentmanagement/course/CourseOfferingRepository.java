package com.example.studentmanagement.course;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CourseOfferingRepository
		extends JpaRepository<CourseOffering, Long>, JpaSpecificationExecutor<CourseOffering> {

	boolean existsBySchoolClassIdAndAcademicYearAndSemesterAndCourseId(long classId, int academicYear,
			Semester semester, long courseId);

	List<CourseOffering> findBySchoolClassIdInAndAcademicYearAndSemesterOrderByCourseCourseCodeAsc(
			Collection<Long> classIds, int academicYear, Semester semester);
}
