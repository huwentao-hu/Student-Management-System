package com.example.studentmanagement.common;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.studentmanagement.auth.AccessDeniedException;
import com.example.studentmanagement.auth.DuplicateAccountException;
import com.example.studentmanagement.auth.InvalidCredentialsException;
import com.example.studentmanagement.auth.InvalidAccountRequestException;
import com.example.studentmanagement.auth.UnauthorizedException;
import com.example.studentmanagement.attendance.AttendanceRecordNotFoundException;
import com.example.studentmanagement.attendance.AttendanceSessionNotFoundException;
import com.example.studentmanagement.attendance.DuplicateAttendanceSessionException;
import com.example.studentmanagement.attendance.InvalidAttendanceException;
import com.example.studentmanagement.course.CourseNotFoundException;
import com.example.studentmanagement.course.CourseOfferingNotFoundException;
import com.example.studentmanagement.course.DuplicateCourseException;
import com.example.studentmanagement.course.DuplicateCourseOfferingException;
import com.example.studentmanagement.course.InvalidCourseOfferingException;
import com.example.studentmanagement.grade.GradeNotFoundException;
import com.example.studentmanagement.grade.InvalidGradeException;
import com.example.studentmanagement.schoolclass.DuplicateSchoolClassException;
import com.example.studentmanagement.schoolclass.InvalidHomeroomTeacherException;
import com.example.studentmanagement.schoolclass.InvalidClassAssignmentException;
import com.example.studentmanagement.schoolclass.SchoolClassNotFoundException;
import com.example.studentmanagement.student.StudentNotFoundException;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(StudentNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(StudentNotFoundException exception) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(DuplicateAccountException.class)
	public ResponseEntity<ApiError> handleConflict(DuplicateAccountException exception) {
		return error(HttpStatus.CONFLICT, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(DuplicateSchoolClassException.class)
	public ResponseEntity<ApiError> handleConflict(DuplicateSchoolClassException exception) {
		return error(HttpStatus.CONFLICT, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(DuplicateCourseException.class)
	public ResponseEntity<ApiError> handleConflict(DuplicateCourseException exception) {
		return error(HttpStatus.CONFLICT, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(DuplicateCourseOfferingException.class)
	public ResponseEntity<ApiError> handleConflict(DuplicateCourseOfferingException exception) {
		return error(HttpStatus.CONFLICT, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(DuplicateAttendanceSessionException.class)
	public ResponseEntity<ApiError> handleConflict(DuplicateAttendanceSessionException exception) {
		return error(HttpStatus.CONFLICT, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(InvalidAccountRequestException.class)
	public ResponseEntity<ApiError> handleBadRequest(InvalidAccountRequestException exception) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(InvalidHomeroomTeacherException.class)
	public ResponseEntity<ApiError> handleBadRequest(InvalidHomeroomTeacherException exception) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(InvalidClassAssignmentException.class)
	public ResponseEntity<ApiError> handleBadRequest(InvalidClassAssignmentException exception) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(InvalidCourseOfferingException.class)
	public ResponseEntity<ApiError> handleBadRequest(InvalidCourseOfferingException exception) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(InvalidGradeException.class)
	public ResponseEntity<ApiError> handleBadRequest(InvalidGradeException exception) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(InvalidAttendanceException.class)
	public ResponseEntity<ApiError> handleBadRequest(InvalidAttendanceException exception) {
		return error(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(SchoolClassNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(SchoolClassNotFoundException exception) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(CourseNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(CourseNotFoundException exception) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(CourseOfferingNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(CourseOfferingNotFoundException exception) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(GradeNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(GradeNotFoundException exception) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(AttendanceSessionNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(AttendanceSessionNotFoundException exception) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(AttendanceRecordNotFoundException.class)
	public ResponseEntity<ApiError> handleNotFound(AttendanceRecordNotFoundException exception) {
		return error(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	public ResponseEntity<ApiError> handleUnauthorized(InvalidCredentialsException exception) {
		return error(HttpStatus.UNAUTHORIZED, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(UnauthorizedException.class)
	public ResponseEntity<ApiError> handleUnauthorized(UnauthorizedException exception) {
		return error(HttpStatus.UNAUTHORIZED, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiError> handleForbidden(AccessDeniedException exception) {
		return error(HttpStatus.FORBIDDEN, exception.getMessage(), Map.of());
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors()
			.forEach(error -> fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
		return error(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException exception) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		exception.getConstraintViolations()
			.forEach(violation -> fieldErrors.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage()));
		return error(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
	}

	private ResponseEntity<ApiError> error(HttpStatus status, String message, Map<String, String> fieldErrors) {
		return ResponseEntity.status(status)
			.body(new ApiError(Instant.now(), status.value(), message, fieldErrors));
	}
}
