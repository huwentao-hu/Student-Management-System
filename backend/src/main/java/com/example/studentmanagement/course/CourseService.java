package com.example.studentmanagement.course;

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
public class CourseService {

	private final CourseRepository courseRepository;

	public CourseService(CourseRepository courseRepository) {
		this.courseRepository = courseRepository;
	}

	@Transactional
	public CourseResponse create(CreateCourseRequest request) {
		String name = request.name().trim();
		if (courseRepository.existsByName(name)) {
			throw new DuplicateCourseException(name);
		}
		CourseStatus status = request.status() == null ? CourseStatus.ACTIVE : request.status();
		Course course = new Course(temporaryCourseCode(), name, request.credits(), status);
		courseRepository.saveAndFlush(course);
		course.assignGeneratedCourseCode("C%08d".formatted(course.getId()));
		return CourseResponse.from(course);
	}

	public CourseResponse getById(long id) {
		return courseRepository.findById(id)
			.map(CourseResponse::from)
			.orElseThrow(() -> new CourseNotFoundException(id));
	}

	public PageResponse<CourseResponse> search(String keyword, CourseStatus status, int page, int size) {
		Specification<Course> specification = (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.conjunction();
			if (keyword != null && !keyword.isBlank()) {
				String pattern = "%" + keyword.trim().toLowerCase() + "%";
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.or(
						criteriaBuilder.like(criteriaBuilder.lower(root.get("courseCode")), pattern),
						criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), pattern)));
			}
			if (status != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("status"), status));
			}
			return predicate;
		};
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by("courseCode").ascending());
		return PageResponse.from(courseRepository.findAll(specification, pageRequest).map(CourseResponse::from));
	}

	private String temporaryCourseCode() {
		return "T" + UUID.randomUUID().toString().replace("-", "").substring(0, 15);
	}
}
