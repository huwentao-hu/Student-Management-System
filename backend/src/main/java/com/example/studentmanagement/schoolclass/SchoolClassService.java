package com.example.studentmanagement.schoolclass;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.studentmanagement.auth.UserAccount;
import com.example.studentmanagement.auth.UserAccountRepository;
import com.example.studentmanagement.auth.UserRole;
import com.example.studentmanagement.common.PageResponse;

import jakarta.persistence.criteria.Predicate;

@Service
@Transactional(readOnly = true)
public class SchoolClassService {

	private final SchoolClassRepository schoolClassRepository;
	private final UserAccountRepository userAccountRepository;

	public SchoolClassService(SchoolClassRepository schoolClassRepository, UserAccountRepository userAccountRepository) {
		this.schoolClassRepository = schoolClassRepository;
		this.userAccountRepository = userAccountRepository;
	}

	@Transactional
	public SchoolClassResponse create(CreateSchoolClassRequest request) {
		String name = request.name().trim();
		if (schoolClassRepository.existsByEntryYearAndName(request.entryYear(), name)) {
			throw new DuplicateSchoolClassException(request.entryYear(), name);
		}
		UserAccount teacher = userAccountRepository.findById(request.homeroomTeacherId())
			.filter(account -> account.getRole() == UserRole.TEACHER && account.isEnabled())
			.orElseThrow(() -> new InvalidHomeroomTeacherException("Homeroom teacher must be an enabled teacher account"));
		return SchoolClassResponse.from(schoolClassRepository.save(new SchoolClass(name, request.entryYear(), teacher)));
	}

	public SchoolClassResponse getById(long id) {
		return schoolClassRepository.findById(id)
			.map(SchoolClassResponse::from)
			.orElseThrow(() -> new SchoolClassNotFoundException(id));
	}

	public PageResponse<SchoolClassResponse> search(String keyword, Integer entryYear, int page, int size) {
		Specification<SchoolClass> specification = (root, query, criteriaBuilder) -> {
			Predicate predicate = criteriaBuilder.conjunction();
			if (keyword != null && !keyword.isBlank()) {
				predicate = criteriaBuilder.and(predicate,
						criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
								"%" + keyword.trim().toLowerCase() + "%"));
			}
			if (entryYear != null) {
				predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("entryYear"), entryYear));
			}
			return predicate;
		};
		PageRequest pageRequest = PageRequest.of(page, size,
				Sort.by("entryYear").descending().and(Sort.by("name").ascending()));
		return PageResponse.from(schoolClassRepository.findAll(specification, pageRequest).map(SchoolClassResponse::from));
	}
}
