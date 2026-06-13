package com.example.studentmanagement.auth;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

	Optional<UserAccount> findByUsername(String username);

	boolean existsByUsername(String username);

	boolean existsByStudentId(Long studentId);

	List<UserAccount> findByRoleAndEnabledTrueOrderByUsernameAsc(UserRole role);
}
