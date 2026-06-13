package com.example.studentmanagement.schoolclass;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.studentmanagement.student.Student;
import com.example.studentmanagement.student.StudentNotFoundException;
import com.example.studentmanagement.student.StudentRepository;

@Service
@Transactional(readOnly = true)
public class StudentClassAssignmentService {

	private final StudentClassAssignmentRepository assignmentRepository;
	private final StudentRepository studentRepository;
	private final SchoolClassRepository schoolClassRepository;

	public StudentClassAssignmentService(StudentClassAssignmentRepository assignmentRepository,
			StudentRepository studentRepository, SchoolClassRepository schoolClassRepository) {
		this.assignmentRepository = assignmentRepository;
		this.studentRepository = studentRepository;
		this.schoolClassRepository = schoolClassRepository;
	}

	@Transactional
	public StudentClassAssignmentResponse assign(long studentId, AssignStudentClassRequest request) {
		Student student = findStudentForUpdate(studentId);
		SchoolClass schoolClass = schoolClassRepository.findById(request.classId())
			.orElseThrow(() -> new SchoolClassNotFoundException(request.classId()));
		List<StudentClassAssignment> history = assignmentRepository.findByStudentIdOrderByStartDateDesc(studentId);
		StudentClassAssignment current = history.stream()
			.filter(assignment -> assignment.getEndDate() == null)
			.findFirst()
			.orElse(null);
		if (current != null) {
			validateEffectiveDate(current, request.effectiveDate());
			if (current.getSchoolClass().getId().equals(request.classId())) {
				throw new InvalidClassAssignmentException("Student is already assigned to this class");
			}
		}
		validateNoOverlap(history, current, request.effectiveDate());
		if (current != null) {
			current.close(request.effectiveDate().minusDays(1));
		}
		return StudentClassAssignmentResponse.from(
				assignmentRepository.save(new StudentClassAssignment(student, schoolClass, request.effectiveDate())));
	}

	@Transactional
	public StudentClassAssignmentResponse leave(long studentId, LeaveStudentClassRequest request) {
		findStudentForUpdate(studentId);
		StudentClassAssignment current = assignmentRepository.findByStudentIdAndEndDateIsNull(studentId)
			.orElseThrow(() -> new InvalidClassAssignmentException("Student is not currently assigned to a class"));
		validateEffectiveDate(current, request.effectiveDate());
		current.close(request.effectiveDate().minusDays(1));
		return StudentClassAssignmentResponse.from(current);
	}

	public List<StudentClassAssignmentResponse> history(long studentId) {
		findStudent(studentId);
		return assignmentRepository.findByStudentIdOrderByStartDateDesc(studentId)
			.stream()
			.map(StudentClassAssignmentResponse::from)
			.toList();
	}

	private Student findStudent(long studentId) {
		return studentRepository.findById(studentId)
			.orElseThrow(() -> new StudentNotFoundException("Student not found: " + studentId));
	}

	private Student findStudentForUpdate(long studentId) {
		return studentRepository.findByIdForUpdate(studentId)
			.orElseThrow(() -> new StudentNotFoundException("Student not found: " + studentId));
	}

	private void validateNoOverlap(List<StudentClassAssignment> history, StudentClassAssignment current,
			LocalDate effectiveDate) {
		boolean overlaps = history.stream()
			.filter(assignment -> assignment != current)
			.anyMatch(assignment -> assignment.getEndDate() == null || !assignment.getEndDate().isBefore(effectiveDate));
		if (overlaps) {
			throw new InvalidClassAssignmentException("New class assignment overlaps existing assignment history");
		}
	}

	private void validateEffectiveDate(StudentClassAssignment current, LocalDate effectiveDate) {
		if (!effectiveDate.isAfter(current.getStartDate())) {
			throw new InvalidClassAssignmentException("Effective date must be after the current assignment start date");
		}
	}
}
