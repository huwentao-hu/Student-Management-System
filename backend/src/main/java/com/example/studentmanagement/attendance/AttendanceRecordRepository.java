package com.example.studentmanagement.attendance;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.studentmanagement.course.Semester;

public interface AttendanceRecordRepository
		extends JpaRepository<AttendanceRecord, Long>, JpaSpecificationExecutor<AttendanceRecord> {

	Optional<AttendanceRecord> findByAttendanceSessionIdAndStudentId(long attendanceSessionId, long studentId);

	@Query("""
			select count(record) as recordedCount,
				sum(case when record.status = com.example.studentmanagement.attendance.AttendanceStatus.PRESENT then 1 else 0 end) as presentCount,
				sum(case when record.status = com.example.studentmanagement.attendance.AttendanceStatus.LATE then 1 else 0 end) as lateCount,
				sum(case when record.status = com.example.studentmanagement.attendance.AttendanceStatus.EXCUSED then 1 else 0 end) as excusedCount,
				sum(case when record.status = com.example.studentmanagement.attendance.AttendanceStatus.ABSENT then 1 else 0 end) as absentCount
			from AttendanceRecord record
			where record.attendanceSession.courseOffering.id = :courseOfferingId
			""")
	AttendanceAggregate aggregateByCourseOfferingId(@Param("courseOfferingId") long courseOfferingId);

	@Query("""
			select count(record) as recordedCount,
				sum(case when record.status = com.example.studentmanagement.attendance.AttendanceStatus.PRESENT then 1 else 0 end) as presentCount,
				sum(case when record.status = com.example.studentmanagement.attendance.AttendanceStatus.LATE then 1 else 0 end) as lateCount,
				sum(case when record.status = com.example.studentmanagement.attendance.AttendanceStatus.EXCUSED then 1 else 0 end) as excusedCount,
				sum(case when record.status = com.example.studentmanagement.attendance.AttendanceStatus.ABSENT then 1 else 0 end) as absentCount
			from AttendanceRecord record
			where record.student.id = :studentId
				and record.attendanceSession.courseOffering.academicYear = :academicYear
				and record.attendanceSession.courseOffering.semester = :semester
			""")
	AttendanceAggregate aggregateByStudentTerm(@Param("studentId") long studentId, @Param("academicYear") int academicYear,
			@Param("semester") Semester semester);
}
