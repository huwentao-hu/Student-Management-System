package com.example.studentmanagement.attendance;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AttendanceSessionRepository
		extends JpaRepository<AttendanceSession, Long>, JpaSpecificationExecutor<AttendanceSession> {

	boolean existsByCourseOfferingIdAndSessionDate(long courseOfferingId, LocalDate sessionDate);

	long countByCourseOfferingId(long courseOfferingId);
}
