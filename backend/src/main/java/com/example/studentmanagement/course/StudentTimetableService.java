package com.example.studentmanagement.course;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.studentmanagement.schoolclass.StudentClassAssignmentRepository;
import com.example.studentmanagement.student.StudentNotFoundException;
import com.example.studentmanagement.student.StudentRepository;

@Service
@Transactional(readOnly = true)
public class StudentTimetableService {

	private final StudentRepository studentRepository;
	private final StudentClassAssignmentRepository assignmentRepository;
	private final CourseOfferingRepository courseOfferingRepository;

	public StudentTimetableService(StudentRepository studentRepository,
			StudentClassAssignmentRepository assignmentRepository, CourseOfferingRepository courseOfferingRepository) {
		this.studentRepository = studentRepository;
		this.assignmentRepository = assignmentRepository;
		this.courseOfferingRepository = courseOfferingRepository;
	}

	public StudentTimetableResponse get(long studentId, int academicYear, Semester semester) {
		if (!studentRepository.existsById(studentId)) {
			throw new StudentNotFoundException("Student not found: " + studentId);
		}
		var periodStart = AcademicTermDates.start(academicYear, semester);
		var periodEnd = AcademicTermDates.end(academicYear, semester);
		List<Long> classIds = assignmentRepository.findOverlappingPeriod(studentId, periodStart, periodEnd)
			.stream()
			.map(assignment -> assignment.getSchoolClass().getId())
			.distinct()
			.toList();
		List<CourseOfferingResponse> offerings = classIds.isEmpty() ? List.of()
				: courseOfferingRepository
					.findBySchoolClassIdInAndAcademicYearAndSemesterOrderByCourseCourseCodeAsc(classIds, academicYear,
							semester)
					.stream()
					.map(CourseOfferingResponse::from)
					.toList();
		return new StudentTimetableResponse(studentId, academicYear, semester, classIds, offerings);
	}

}
