package com.example.studentmanagement.student;

import org.springframework.stereotype.Service;

import com.example.studentmanagement.auth.AccessDeniedException;
import com.example.studentmanagement.auth.AuthenticatedUser;
import com.example.studentmanagement.auth.UserRole;

@Service
public class StudentAuthorizationService {

	public void requireAdmin(AuthenticatedUser user) {
		if (user.role() != UserRole.ADMIN) {
			throw new AccessDeniedException("Administrator role is required");
		}
	}

	public void requireCanList(AuthenticatedUser user) {
		if (user.role() == UserRole.STUDENT) {
			throw new AccessDeniedException("Students cannot list all student records");
		}
	}

	public void requireCanRead(AuthenticatedUser user, StudentResponse student) {
		if (user.role() == UserRole.STUDENT && !student.id().equals(user.studentId())) {
			throw new AccessDeniedException("Students can only view their own record");
		}
	}
}
