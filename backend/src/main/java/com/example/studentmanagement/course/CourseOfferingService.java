package com.example.studentmanagement.course;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.studentmanagement.auth.UserAccount;
import com.example.studentmanagement.auth.UserAccountRepository;
import com.example.studentmanagement.auth.UserRole;
import com.example.studentmanagement.common.PageResponse;
import com.example.studentmanagement.schoolclass.SchoolClass;
import com.example.studentmanagement.schoolclass.SchoolClassNotFoundException;
import com.example.studentmanagement.schoolclass.SchoolClassRepository;

import jakarta.persistence.criteria.Predicate;

@Service
@Transactional(readOnly = true)
public class CourseOfferingService {

	private final CourseOfferingRepository courseOfferingRepository;
	private final CourseRepository courseRepository;
	private final SchoolClassRepository schoolClassRepository;
	private final UserAccountRepository userAccountRepository;

	public CourseOfferingService(CourseOfferingRepository courseOfferingRepository, CourseRepository courseRepository,
			SchoolClassRepository schoolClassRepository, UserAccountRepository userAccountRepository) {
		this.courseOfferingRepository = courseOfferingRepository;
		this.courseRepository = courseRepository;
		this.schoolClassRepository = schoolClassRepository;
		this.userAccountRepository = userAccountRepository;
	}

	@Transactional
	public CourseOfferingResponse create(CreateCourseOfferingRequest request) {
		Course course = courseRepository.findById(request.courseId())
			.orElseThrow(() -> new CourseNotFoundException(request.courseId()));
		if (course.getStatus() != CourseStatus.ACTIVE) {
			throw new InvalidCourseOfferingException("Course must be active");
		}
		SchoolClass schoolClass = schoolClassRepository.findById(request.classId())
			.orElseThrow(() -> new SchoolClassNotFoundException(request.classId()));
		UserAccount teacher = userAccountRepository.findById(request.teacherId())
			.filter(account -> account.getRole() == UserRole.TEACHER && account.isEnabled())
			.orElseThrow(() -> new InvalidCourseOfferingException("Teacher must be an enabled teacher account"));
		if (courseOfferingRepository.existsBySchoolClassIdAndAcademicYearAndSemesterAndCourseId(request.classId(),
				request.academicYear(), request.semester(), request.courseId())) {
			throw new DuplicateCourseOfferingException();
		}
		return CourseOfferingResponse.from(courseOfferingRepository.save(
				new CourseOffering(course, schoolClass, teacher, request.academicYear(), request.semester())));
	}

	public CourseOfferingResponse getById(long id) {
		return courseOfferingRepository.findById(id)
			.map(CourseOfferingResponse::from)
			.orElseThrow(() -> new CourseOfferingNotFoundException(id));
	}

	public PageResponse<CourseOfferingResponse> search(Long courseId, Long classId, Long teacherId, Integer academicYear,
			Semester semester, int page, int size) {
		Specification<CourseOffering> specification = (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.conjunction();
			if (courseId != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("course").get("id"), courseId));
			}
			if (classId != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("schoolClass").get("id"), classId));
			}
			if (teacherId != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("teacher").get("id"), teacherId));
			}
			if (academicYear != null) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.equal(root.get("academicYear"), academicYear));
			}
			if (semester != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("semester"), semester));
			}
			return predicate;
		};
		PageRequest pageRequest = PageRequest.of(page, size,
				Sort.by("academicYear").descending().and(Sort.by("semester")).and(Sort.by("id")));
		return PageResponse.from(
				courseOfferingRepository.findAll(specification, pageRequest).map(CourseOfferingResponse::from));
	}
}
