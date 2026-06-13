package com.example.studentmanagement.schoolclass;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentClassAssignmentRepository extends JpaRepository<StudentClassAssignment, Long> {

	Optional<StudentClassAssignment> findByStudentIdAndEndDateIsNull(Long studentId);

	List<StudentClassAssignment> findByStudentIdOrderByStartDateDesc(Long studentId);

	@Query("""
			select assignment
			from StudentClassAssignment assignment
			where assignment.student.id = :studentId
			  and assignment.startDate <= :periodEnd
			  and (assignment.endDate is null or assignment.endDate >= :periodStart)
			order by assignment.startDate
			""")
	List<StudentClassAssignment> findOverlappingPeriod(@Param("studentId") Long studentId,
			@Param("periodStart") LocalDate periodStart, @Param("periodEnd") LocalDate periodEnd);
}
