package com.example.studentmanagement.auth;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuthTokenRepository extends JpaRepository<AuthToken, String> {

	@Query("""
			select token from AuthToken token
			join fetch token.userAccount account
			left join fetch account.student
			where token.tokenHash = :tokenHash and token.expiresAt > :now
			""")
	Optional<AuthToken> findValidToken(@Param("tokenHash") String tokenHash, @Param("now") Instant now);

	@Modifying
	@Query("delete from AuthToken token where token.expiresAt <= :now")
	int deleteExpiredTokens(@Param("now") Instant now);
}
