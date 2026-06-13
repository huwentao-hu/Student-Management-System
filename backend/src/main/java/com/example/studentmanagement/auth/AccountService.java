package com.example.studentmanagement.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.studentmanagement.student.Student;
import com.example.studentmanagement.student.StudentNotFoundException;
import com.example.studentmanagement.student.StudentRepository;

@Service
@Transactional
public class AccountService {

	private final UserAccountRepository userAccountRepository;
	private final StudentRepository studentRepository;
	private final PasswordEncoder passwordEncoder;

	public AccountService(UserAccountRepository userAccountRepository, StudentRepository studentRepository,
			PasswordEncoder passwordEncoder) {
		this.userAccountRepository = userAccountRepository;
		this.studentRepository = studentRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public AccountResponse create(CreateAccountRequest request) {
		String username = request.username().trim();
		if (userAccountRepository.existsByUsername(username)) {
			throw new DuplicateAccountException("Username already exists: " + username);
		}

		Student student = validateAndFindStudent(request.role(), request.studentId());
		UserAccount account = new UserAccount(username, passwordEncoder.encode(request.password()), request.role(), student);
		return AccountResponse.from(userAccountRepository.save(account));
	}

	private Student validateAndFindStudent(UserRole role, Long studentId) {
		if (role == UserRole.ADMIN) {
			throw new InvalidAccountRequestException("Administrator accounts cannot be created through this endpoint");
		}
		if (role == UserRole.TEACHER) {
			if (studentId != null) {
				throw new InvalidAccountRequestException("Teacher accounts cannot be linked to a student");
			}
			return null;
		}
		if (studentId == null) {
			throw new InvalidAccountRequestException("Student accounts require studentId");
		}
		if (userAccountRepository.existsByStudentId(studentId)) {
			throw new DuplicateAccountException("Student already has an account: " + studentId);
		}
		return studentRepository.findById(studentId)
			.orElseThrow(() -> new StudentNotFoundException("Student not found: " + studentId));
	}
}
