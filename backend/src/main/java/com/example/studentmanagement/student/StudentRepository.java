package com.example.studentmanagement.student;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {

	Optional<Student> findByStudentNumber(String studentNumber);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select student from Student student where student.id = :id")
	Optional<Student> findByIdForUpdate(@Param("id") Long id);
}
