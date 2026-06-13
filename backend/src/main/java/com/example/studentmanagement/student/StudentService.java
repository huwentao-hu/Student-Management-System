package com.example.studentmanagement.student;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.studentmanagement.common.PageResponse;

import jakarta.persistence.criteria.Predicate;

@Service
@Transactional(readOnly = true)
public class StudentService {

	private final StudentRepository studentRepository;

	public StudentService(StudentRepository studentRepository) {
		this.studentRepository = studentRepository;
	}

	@Transactional
	public StudentResponse create(CreateStudentRequest request) {
		Student student = new Student(temporaryStudentNumber(), request.name().trim(), normalize(request.gender()),
				request.dateOfBirth(), normalize(request.phone()), normalize(request.email()), request.enrollmentDate());
		studentRepository.saveAndFlush(student);
		student.assignGeneratedStudentNumber(generateStudentNumber(student.getId(), request.enrollmentDate()));
		return StudentResponse.from(student);
	}

	@Transactional
	public StudentResponse update(long id, UpdateStudentRequest request) {
		Student student = studentRepository.findById(id)
			.orElseThrow(() -> new StudentNotFoundException("Student not found: " + id));
		student.update(request.name().trim(), normalize(request.gender()), request.dateOfBirth(), normalize(request.phone()),
				normalize(request.email()), request.enrollmentDate(), request.status());
		return StudentResponse.from(student);
	}

	public StudentResponse getById(long id) {
		return studentRepository.findById(id)
			.map(StudentResponse::from)
			.orElseThrow(() -> new StudentNotFoundException("Student not found: " + id));
	}

	public StudentResponse getByStudentNumber(String studentNumber) {
		return studentRepository.findByStudentNumber(studentNumber)
			.map(StudentResponse::from)
			.orElseThrow(() -> new StudentNotFoundException("Student not found: " + studentNumber));
	}

	public PageResponse<StudentResponse> search(String keyword, StudentStatus status, int page, int size) {
		Specification<Student> specification = (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.conjunction();
			String normalizedKeyword = normalize(keyword);
			if (normalizedKeyword != null) {
				String pattern = "%" + normalizedKeyword.toLowerCase() + "%";
				Predicate studentNumberMatches = criteriaBuilder.like(criteriaBuilder.lower(root.get("studentNumber")),
						pattern);
				Predicate nameMatches = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern);
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(studentNumberMatches, nameMatches));
			}
			if (status != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
			}
			return predicate;
		};

		PageRequest pageRequest = PageRequest.of(page, size, Sort.by("studentNumber").ascending());
		return PageResponse.from(studentRepository.findAll(specification, pageRequest).map(StudentResponse::from));
	}

	private String normalize(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	private String temporaryStudentNumber() {
		return "TMP-" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
	}

	private String generateStudentNumber(long id, LocalDate enrollmentDate) {
		int enrollmentYear = enrollmentDate == null ? LocalDate.now().getYear() : enrollmentDate.getYear();
		return "%d%08d".formatted(enrollmentYear, id);
	}
}
