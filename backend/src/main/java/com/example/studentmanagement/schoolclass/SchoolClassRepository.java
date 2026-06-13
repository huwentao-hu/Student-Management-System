package com.example.studentmanagement.schoolclass;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SchoolClassRepository extends JpaRepository<SchoolClass, Long>, JpaSpecificationExecutor<SchoolClass> {

	boolean existsByEntryYearAndName(int entryYear, String name);
}
