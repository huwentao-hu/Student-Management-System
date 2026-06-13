package com.example.studentmanagement;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.studentmanagement.auth.AuthTokenRepository;
import com.example.studentmanagement.auth.AuthService;
import com.example.studentmanagement.auth.AuthToken;
import com.example.studentmanagement.auth.AuthenticatedUser;
import com.example.studentmanagement.auth.LoginRequest;
import com.example.studentmanagement.auth.UserAccount;
import com.example.studentmanagement.auth.UserAccountRepository;
import com.example.studentmanagement.auth.UserRole;
import com.example.studentmanagement.attendance.AttendanceSession;
import com.example.studentmanagement.attendance.AttendanceRecord;
import com.example.studentmanagement.attendance.AttendanceRecordRepository;
import com.example.studentmanagement.attendance.AttendanceService;
import com.example.studentmanagement.attendance.AttendanceSessionRepository;
import com.example.studentmanagement.attendance.AttendanceStatus;
import com.example.studentmanagement.attendance.AttendanceUpsertResult;
import com.example.studentmanagement.attendance.UpsertAttendanceRecordRequest;
import com.example.studentmanagement.course.Course;
import com.example.studentmanagement.course.CourseOffering;
import com.example.studentmanagement.course.CourseOfferingRepository;
import com.example.studentmanagement.course.CourseRepository;
import com.example.studentmanagement.course.CourseStatus;
import com.example.studentmanagement.course.Semester;
import com.example.studentmanagement.grade.Grade;
import com.example.studentmanagement.grade.GradeRepository;
import com.example.studentmanagement.grade.GradeService;
import com.example.studentmanagement.grade.GradeUpsertResult;
import com.example.studentmanagement.grade.UpsertGradeRequest;
import com.example.studentmanagement.student.Student;
import com.example.studentmanagement.student.CreateStudentRequest;
import com.example.studentmanagement.student.StudentRepository;
import com.example.studentmanagement.student.StudentResponse;
import com.example.studentmanagement.student.StudentService;
import com.example.studentmanagement.schoolclass.SchoolClass;
import com.example.studentmanagement.schoolclass.SchoolClassRepository;
import com.example.studentmanagement.schoolclass.AssignStudentClassRequest;
import com.example.studentmanagement.schoolclass.StudentClassAssignment;
import com.example.studentmanagement.schoolclass.StudentClassAssignmentRepository;
import com.example.studentmanagement.schoolclass.StudentClassAssignmentService;

@SpringBootTest
@AutoConfigureMockMvc
class StudentManagementApplicationTests {

	private final MockMvc mockMvc;
	private final UserAccountRepository userAccountRepository;
	private final AuthTokenRepository authTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthService authService;
	private final StudentRepository studentRepository;
	private final StudentService studentService;
	private final SchoolClassRepository schoolClassRepository;
	private final CourseRepository courseRepository;
	private final StudentClassAssignmentRepository studentClassAssignmentRepository;
	private final StudentClassAssignmentService studentClassAssignmentService;
	private final CourseOfferingRepository courseOfferingRepository;
	private final AttendanceSessionRepository attendanceSessionRepository;
	private final AttendanceRecordRepository attendanceRecordRepository;
	private final AttendanceService attendanceService;
	private final GradeRepository gradeRepository;
	private final GradeService gradeService;

	@Autowired
	StudentManagementApplicationTests(MockMvc mockMvc, UserAccountRepository userAccountRepository,
			AuthTokenRepository authTokenRepository, PasswordEncoder passwordEncoder, AuthService authService,
			StudentRepository studentRepository, StudentService studentService, SchoolClassRepository schoolClassRepository,
			CourseRepository courseRepository, StudentClassAssignmentRepository studentClassAssignmentRepository,
			StudentClassAssignmentService studentClassAssignmentService, CourseOfferingRepository courseOfferingRepository,
			AttendanceSessionRepository attendanceSessionRepository, AttendanceRecordRepository attendanceRecordRepository,
			AttendanceService attendanceService, GradeRepository gradeRepository, GradeService gradeService) {
		this.mockMvc = mockMvc;
		this.userAccountRepository = userAccountRepository;
		this.authTokenRepository = authTokenRepository;
		this.passwordEncoder = passwordEncoder;
		this.authService = authService;
		this.studentRepository = studentRepository;
		this.studentService = studentService;
		this.schoolClassRepository = schoolClassRepository;
		this.courseRepository = courseRepository;
		this.studentClassAssignmentRepository = studentClassAssignmentRepository;
		this.studentClassAssignmentService = studentClassAssignmentService;
		this.courseOfferingRepository = courseOfferingRepository;
		this.attendanceSessionRepository = attendanceSessionRepository;
		this.attendanceRecordRepository = attendanceRecordRepository;
		this.attendanceService = attendanceService;
		this.gradeRepository = gradeRepository;
		this.gradeService = gradeService;
	}

	@Test
	void createsAndQueriesStudent() throws Exception {
		String request = """
				{
				  "name": "Zhang San",
				  "gender": "MALE",
				  "dateOfBirth": "2008-05-12",
				  "email": "zhangsan@example.com",
				  "enrollmentDate": "2026-09-01"
				}
				""";

		mockMvc.perform(post("/api/students").header("Authorization", bearer(adminToken()))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", matchesPattern("http://localhost/api/students/\\d+")))
			.andExpect(jsonPath("$.studentNumber", matchesPattern("2026\\d{8}")))
			.andExpect(jsonPath("$.status", is("ACTIVE")));

		mockMvc.perform(get("/api/students").header("Authorization", bearer(adminToken()))
				.param("keyword", "Zhang San"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].name", is("Zhang San")));
	}

	@Test
	void generatesUniqueStudentNumbersConcurrently() throws Exception {
		try (var executor = Executors.newFixedThreadPool(6)) {
			List<Future<StudentResponse>> futures = IntStream.range(0, 6)
				.mapToObj(index -> executor.submit(() -> studentService.create(new CreateStudentRequest(
						"Concurrent Student " + index, null, null, null, null, LocalDate.of(2027, 9, 1)))))
				.toList();
			Set<String> studentNumbers = new java.util.HashSet<>();
			for (Future<StudentResponse> future : futures) {
				studentNumbers.add(future.get().studentNumber());
			}
			org.assertj.core.api.Assertions.assertThat(studentNumbers)
				.hasSize(6)
				.allMatch(number -> number.matches("2027\\d{8}"));
		}
	}

	@Test
	void searchesStudentsWithPagination() throws Exception {
		createStudent("SEARCH-20260002", "Alpha Two");
		createStudent("SEARCH-20260001", "Alpha One");
		createStudent("SEARCH-20260003", "Beta One");

		mockMvc.perform(get("/api/students")
				.header("Authorization", bearer(adminToken()))
				.param("keyword", "alpha")
				.param("status", "ACTIVE")
				.param("page", "0")
				.param("size", "1"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content", hasSize(1)))
			.andExpect(jsonPath("$.content[0].name", is("Alpha Two")))
			.andExpect(jsonPath("$.page", is(0)))
			.andExpect(jsonPath("$.size", is(1)))
			.andExpect(jsonPath("$.totalElements", is(2)))
			.andExpect(jsonPath("$.totalPages", is(2)))
			.andExpect(jsonPath("$.first", is(true)))
			.andExpect(jsonPath("$.last", is(false)));
	}

	@Test
	void rejectsPageSizeAboveLimit() throws Exception {
		mockMvc.perform(get("/api/students").header("Authorization", bearer(adminToken())).param("size", "101"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void logsInWithValidCredentialsAndStoresHashedToken() throws Exception {
		String username = "login-admin";
		userAccountRepository.save(new UserAccount(username, passwordEncoder.encode("StrongPassword123!"),
				UserRole.ADMIN, null));
		long tokensBefore = authTokenRepository.count();

		String request = """
				{"username":"login-admin","password":"StrongPassword123!"}
				""";

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.token", matchesPattern("[A-Za-z0-9_-]{43}")))
			.andExpect(jsonPath("$.username", is(username)))
			.andExpect(jsonPath("$.role", is("ADMIN")))
			.andExpect(jsonPath("$.studentId").doesNotExist());

		org.assertj.core.api.Assertions.assertThat(authTokenRepository.count()).isEqualTo(tokensBefore + 1);
	}

	@Test
	void rejectsInvalidCredentials() throws Exception {
		String request = """
				{"username":"missing-user","password":"wrong-password"}
				""";

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message", is("Invalid username or password")));
	}

	@Test
	void requiresAuthenticationForStudentEndpoints() throws Exception {
		mockMvc.perform(get("/api/students"))
			.andExpect(status().isUnauthorized())
			.andExpect(jsonPath("$.message", is("Bearer authentication token is required")));
	}

	@Test
	void healthCheckIsPublicAndChecksDatabase() throws Exception {
		mockMvc.perform(get("/api/health"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status", is("UP")))
			.andExpect(jsonPath("$.database", is("UP")))
			.andExpect(jsonPath("$.checkedAt").exists());
	}

	@Test
	void logoutRevokesOnlyCurrentToken() throws Exception {
		UserAccount account = saveAccount(UserRole.TEACHER, null);
		String firstToken = authService.login(new LoginRequest(account.getUsername(), "StrongPassword123!")).token();
		String secondToken = authService.login(new LoginRequest(account.getUsername(), "StrongPassword123!")).token();

		mockMvc.perform(post("/api/auth/logout").header("Authorization", bearer(firstToken)))
			.andExpect(status().isNoContent());
		mockMvc.perform(get("/api/students").header("Authorization", bearer(firstToken)))
			.andExpect(status().isUnauthorized());
		mockMvc.perform(get("/api/students").header("Authorization", bearer(secondToken)))
			.andExpect(status().isOk());
	}

	@Test
	void adminCleansExpiredTokensWithoutRevokingValidTokens() throws Exception {
		authService.cleanupExpiredTokens();
		UserAccount account = saveAccount(UserRole.TEACHER, null);
		authTokenRepository.save(new AuthToken("expired-" + UUID.randomUUID().toString().replace("-", ""), account,
				Instant.now().minusSeconds(60)));
		String adminToken = adminToken();
		long tokensBefore = authTokenRepository.count();

		mockMvc.perform(post("/api/auth/tokens/cleanup").header("Authorization", bearer(adminToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.deletedCount", is(1)));
		assertEquals(tokensBefore - 1, authTokenRepository.count());
		mockMvc.perform(get("/api/students").header("Authorization", bearer(adminToken)))
			.andExpect(status().isOk());
		mockMvc.perform(post("/api/auth/tokens/cleanup")
				.header("Authorization", bearer(tokenForRole(UserRole.TEACHER, null))))
			.andExpect(status().isForbidden());
	}

	@Test
	void corsAllowsConfiguredOriginAndRejectsOtherOrigins() throws Exception {
		mockMvc.perform(options("/api/students").header("Origin", "http://localhost:5173")
				.header("Access-Control-Request-Method", "GET")
				.header("Access-Control-Request-Headers", "Authorization"))
			.andExpect(status().isOk())
			.andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
			.andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("GET")))
			.andExpect(header().string("Access-Control-Allow-Headers",
					org.hamcrest.Matchers.containsStringIgnoringCase("Authorization")));
		mockMvc.perform(options("/api/students").header("Origin", "https://not-allowed.example")
				.header("Access-Control-Request-Method", "GET"))
			.andExpect(status().isForbidden())
			.andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
	}

	@Test
	void teacherCanListStudentsButCannotCreateThem() throws Exception {
		String teacherToken = tokenForRole(UserRole.TEACHER, null);

		mockMvc.perform(get("/api/students").header("Authorization", bearer(teacherToken)))
			.andExpect(status().isOk());

		mockMvc.perform(post("/api/students").header("Authorization", bearer(teacherToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"name\":\"Denied\"}"))
			.andExpect(status().isForbidden())
			.andExpect(jsonPath("$.message", is("Administrator role is required")));
	}

	@Test
	void studentCanOnlyReadOwnRecord() throws Exception {
		Student ownStudent = studentRepository.save(new Student("OWN-" + shortId(), "Own Student", null, null, null,
				null, null));
		Student otherStudent = studentRepository.save(new Student("OTHER-" + shortId(), "Other Student", null, null,
				null, null, null));
		String studentToken = tokenForRole(UserRole.STUDENT, ownStudent);

		mockMvc.perform(get("/api/students/{id}", ownStudent.getId()).header("Authorization", bearer(studentToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id", is(ownStudent.getId().intValue())));

		mockMvc.perform(get("/api/students/{id}", otherStudent.getId()).header("Authorization", bearer(studentToken)))
			.andExpect(status().isForbidden());

		mockMvc.perform(get("/api/students").header("Authorization", bearer(studentToken)))
			.andExpect(status().isForbidden());
	}

	@Test
	void adminCreatesTeacherAndStudentAccountsThatCanLogIn() throws Exception {
		String adminToken = adminToken();
		String teacherUsername = "teacher-" + shortId();
		String teacherRequest = """
				{"username":"%s","password":"TeacherPassword123!","role":"TEACHER"}
				""".formatted(teacherUsername);

		mockMvc.perform(post("/api/accounts").header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON).content(teacherRequest))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.username", is(teacherUsername)))
			.andExpect(jsonPath("$.role", is("TEACHER")))
			.andExpect(jsonPath("$.studentId").doesNotExist());

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"%s\",\"password\":\"TeacherPassword123!\"}".formatted(teacherUsername)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.role", is("TEACHER")));

		Student student = studentRepository.save(new Student("ACCOUNT-" + shortId(), "Account Student", null, null,
				null, null, null));
		String studentUsername = "student-" + shortId();
		String studentRequest = """
				{"username":"%s","password":"StudentPassword123!","role":"STUDENT","studentId":%d}
				""".formatted(studentUsername, student.getId());

		mockMvc.perform(post("/api/accounts").header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON).content(studentRequest))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.role", is("STUDENT")))
			.andExpect(jsonPath("$.studentId", is(student.getId().intValue())));

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"%s\",\"password\":\"StudentPassword123!\"}".formatted(studentUsername)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.studentId", is(student.getId().intValue())));
	}

	@Test
	void onlyAdminCanCreateAccounts() throws Exception {
		String teacherToken = tokenForRole(UserRole.TEACHER, null);
		String request = """
				{"username":"denied-%s","password":"StrongPassword123!","role":"TEACHER"}
				""".formatted(shortId());

		mockMvc.perform(post("/api/accounts").header("Authorization", bearer(teacherToken))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isForbidden());

		mockMvc.perform(post("/api/accounts").contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void rejectsInvalidAndDuplicateAccountCreation() throws Exception {
		String adminToken = adminToken();
		String username = "duplicate-" + shortId();
		String teacherRequest = """
				{"username":"%s","password":"StrongPassword123!","role":"TEACHER"}
				""".formatted(username);

		mockMvc.perform(post("/api/accounts").header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON).content(teacherRequest))
			.andExpect(status().isCreated());
		mockMvc.perform(post("/api/accounts").header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON).content(teacherRequest))
			.andExpect(status().isConflict());

		Student student = studentRepository.save(new Student("DUP-ACCOUNT-" + shortId(), "Duplicate Account Student",
				null, null, null, null, null));
		String firstStudentAccount = """
				{"username":"student-a-%s","password":"StrongPassword123!","role":"STUDENT","studentId":%d}
				""".formatted(shortId(), student.getId());
		String secondStudentAccount = """
				{"username":"student-b-%s","password":"StrongPassword123!","role":"STUDENT","studentId":%d}
				""".formatted(shortId(), student.getId());

		mockMvc.perform(post("/api/accounts").header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON).content(firstStudentAccount))
			.andExpect(status().isCreated());
		mockMvc.perform(post("/api/accounts").header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON).content(secondStudentAccount))
			.andExpect(status().isConflict());

		mockMvc.perform(post("/api/accounts").header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"username\":\"admin-%s\",\"password\":\"StrongPassword123!\",\"role\":\"ADMIN\"}"
					.formatted(shortId())))
			.andExpect(status().isBadRequest());
	}

	@Test
	void onlyAdminCanUpdateStudentAndStudentNumberStaysUnchanged() throws Exception {
		Student student = studentRepository.save(new Student("IMMUTABLE-" + shortId(), "Before Update", null, null,
				null, null, LocalDate.of(2025, 9, 1)));
		String originalStudentNumber = student.getStudentNumber();
		String updateRequest = """
				{
				  "name":"After Update",
				  "gender":"FEMALE",
				  "email":"after@example.com",
				  "enrollmentDate":"2026-09-01",
				  "status":"ACTIVE"
				}
				""";

		mockMvc.perform(put("/api/students/{id}", student.getId()).header("Authorization", bearer(adminToken()))
				.contentType(MediaType.APPLICATION_JSON).content(updateRequest))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name", is("After Update")))
			.andExpect(jsonPath("$.studentNumber", is(originalStudentNumber)));

		mockMvc.perform(put("/api/students/{id}", student.getId())
				.header("Authorization", bearer(tokenForRole(UserRole.TEACHER, null)))
				.contentType(MediaType.APPLICATION_JSON).content(updateRequest))
			.andExpect(status().isForbidden());

		mockMvc.perform(put("/api/students/{id}", student.getId())
				.header("Authorization", bearer(tokenForRole(UserRole.STUDENT, student)))
				.contentType(MediaType.APPLICATION_JSON).content(updateRequest))
			.andExpect(status().isForbidden());
	}

	@Test
	void adminCreatesClassAndTeacherCanQueryIt() throws Exception {
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		String className = "Software Engineering " + shortId();
		String request = """
				{"name":"%s","entryYear":2026,"homeroomTeacherId":%d}
				""".formatted(className, teacher.getId());

		mockMvc.perform(post("/api/classes").header("Authorization", bearer(adminToken()))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name", is(className)))
			.andExpect(jsonPath("$.entryYear", is(2026)))
			.andExpect(jsonPath("$.homeroomTeacherId", is(teacher.getId().intValue())));

		String teacherToken = authService.login(new LoginRequest(teacher.getUsername(), "StrongPassword123!")).token();
		mockMvc.perform(get("/api/classes").header("Authorization", bearer(teacherToken))
				.param("entryYear", "2026").param("keyword", className))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].name", is(className)));
	}

	@Test
	void rejectsDuplicateClassAndInvalidHomeroomTeacher() throws Exception {
		String adminToken = adminToken();
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		String className = "Duplicate Class " + shortId();
		String request = """
				{"name":"%s","entryYear":2027,"homeroomTeacherId":%d}
				""".formatted(className, teacher.getId());

		mockMvc.perform(post("/api/classes").header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isCreated());
		mockMvc.perform(post("/api/classes").header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isConflict());

		UserAccount invalidTeacher = saveAccount(UserRole.ADMIN, null);
		String invalidRequest = """
				{"name":"Invalid Teacher Class","entryYear":2027,"homeroomTeacherId":%d}
				""".formatted(invalidTeacher.getId());
		mockMvc.perform(post("/api/classes").header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON).content(invalidRequest))
			.andExpect(status().isBadRequest());
	}

	@Test
	void onlyStaffCanAccessClassesAndOnlyAdminCanCreate() throws Exception {
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		String request = """
				{"name":"Denied Class %s","entryYear":2028,"homeroomTeacherId":%d}
				""".formatted(shortId(), teacher.getId());
		String teacherToken = authService.login(new LoginRequest(teacher.getUsername(), "StrongPassword123!")).token();

		mockMvc.perform(post("/api/classes").header("Authorization", bearer(teacherToken))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isForbidden());

		Student student = studentRepository.save(new Student("CLASS-STUDENT-" + shortId(), "Class Student", null, null,
				null, null, null));
		String studentToken = tokenForRole(UserRole.STUDENT, student);
		mockMvc.perform(get("/api/classes").header("Authorization", bearer(studentToken)))
			.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/classes"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void adminListsEnabledTeachersForClassSelection() throws Exception {
		UserAccount firstTeacher = saveAccount(UserRole.TEACHER, null);
		UserAccount secondTeacher = saveAccount(UserRole.TEACHER, null);
		saveAccount(UserRole.ADMIN, null);

		mockMvc.perform(get("/api/accounts/teachers").header("Authorization", bearer(adminToken())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$[*].id", hasItems(firstTeacher.getId().intValue(), secondTeacher.getId().intValue())))
			.andExpect(jsonPath("$[*].role", everyItem(is("TEACHER"))))
			.andExpect(jsonPath("$[*].enabled", everyItem(is(true))));

		mockMvc.perform(get("/api/accounts/teachers")
				.header("Authorization", bearer(tokenForRole(UserRole.TEACHER, null))))
			.andExpect(status().isForbidden());
	}

	@Test
	void staffListsOnlyCurrentStudentsInClassRoster() throws Exception {
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass schoolClass = schoolClassRepository.save(new SchoolClass("Roster " + shortId(), 2026, teacher));
		Student currentStudent = studentRepository.save(new Student("ROSTER-CURRENT-" + shortId(), "Current Student",
				null, null, null, null, null));
		Student formerStudent = studentRepository.save(new Student("ROSTER-FORMER-" + shortId(), "Former Student",
				null, null, null, null, null));
		studentClassAssignmentRepository.save(new StudentClassAssignment(currentStudent, schoolClass,
				LocalDate.of(2026, 9, 1)));
		StudentClassAssignment formerAssignment = new StudentClassAssignment(formerStudent, schoolClass,
				LocalDate.of(2026, 9, 1));
		formerAssignment.close(LocalDate.of(2027, 1, 31));
		studentClassAssignmentRepository.save(formerAssignment);
		String teacherToken = authService.login(new LoginRequest(teacher.getUsername(), "StrongPassword123!")).token();

		mockMvc.perform(get("/api/classes/{id}/students", schoolClass.getId())
				.header("Authorization", bearer(teacherToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)))
			.andExpect(jsonPath("$[0].id", is(currentStudent.getId().intValue())));

		mockMvc.perform(get("/api/classes/{id}/students", schoolClass.getId())
				.header("Authorization", bearer(tokenForRole(UserRole.STUDENT, currentStudent))))
			.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/classes/{id}/students", Long.MAX_VALUE)
				.header("Authorization", bearer(adminToken())))
			.andExpect(status().isNotFound());
	}

	@Test
	void adminAssignsTransfersAndRemovesStudentWhileKeepingHistory() throws Exception {
		Student student = studentRepository.save(new Student("ASSIGN-" + shortId(), "Assigned Student", null, null,
				null, null, LocalDate.of(2026, 9, 1)));
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass firstClass = schoolClassRepository.save(new SchoolClass("First " + shortId(), 2026, teacher));
		SchoolClass secondClass = schoolClassRepository.save(new SchoolClass("Second " + shortId(), 2026, teacher));
		String adminToken = adminToken();

		mockMvc.perform(post("/api/students/{id}/class-assignments", student.getId())
				.header("Authorization", bearer(adminToken)).contentType(MediaType.APPLICATION_JSON)
				.content("{\"classId\":%d,\"effectiveDate\":\"2026-09-01\"}".formatted(firstClass.getId())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.classId", is(firstClass.getId().intValue())))
			.andExpect(jsonPath("$.current", is(true)));

		mockMvc.perform(post("/api/students/{id}/class-assignments", student.getId())
				.header("Authorization", bearer(adminToken)).contentType(MediaType.APPLICATION_JSON)
				.content("{\"classId\":%d,\"effectiveDate\":\"2027-02-01\"}".formatted(secondClass.getId())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.classId", is(secondClass.getId().intValue())));

		mockMvc.perform(post("/api/students/{id}/class-assignments/leave", student.getId())
				.header("Authorization", bearer(adminToken)).contentType(MediaType.APPLICATION_JSON)
				.content("{\"effectiveDate\":\"2027-07-01\"}"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.endDate", is("2027-06-30")))
			.andExpect(jsonPath("$.current", is(false)));

		mockMvc.perform(get("/api/students/{id}/class-assignments", student.getId())
				.header("Authorization", bearer(adminToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(2)))
			.andExpect(jsonPath("$[0].classId", is(secondClass.getId().intValue())))
			.andExpect(jsonPath("$[0].endDate", is("2027-06-30")))
			.andExpect(jsonPath("$[1].classId", is(firstClass.getId().intValue())))
			.andExpect(jsonPath("$[1].endDate", is("2027-01-31")));
	}

	@Test
	void enforcesClassAssignmentDatesAndPermissions() throws Exception {
		Student student = studentRepository.save(new Student("ASSIGN-RULE-" + shortId(), "Assignment Rules", null, null,
				null, null, null));
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass schoolClass = schoolClassRepository.save(new SchoolClass("Rules " + shortId(), 2026, teacher));
		String adminToken = adminToken();
		String assignRequest = "{\"classId\":%d,\"effectiveDate\":\"2026-09-01\"}".formatted(schoolClass.getId());

		mockMvc.perform(post("/api/students/{id}/class-assignments", student.getId())
				.header("Authorization", bearer(adminToken)).contentType(MediaType.APPLICATION_JSON).content(assignRequest))
			.andExpect(status().isOk());
		mockMvc.perform(post("/api/students/{id}/class-assignments", student.getId())
				.header("Authorization", bearer(adminToken)).contentType(MediaType.APPLICATION_JSON).content(assignRequest))
			.andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/students/{id}/class-assignments/leave", student.getId())
				.header("Authorization", bearer(adminToken)).contentType(MediaType.APPLICATION_JSON)
				.content("{\"effectiveDate\":\"2026-09-01\"}"))
			.andExpect(status().isBadRequest());

		String teacherToken = authService.login(new LoginRequest(teacher.getUsername(), "StrongPassword123!")).token();
		mockMvc.perform(get("/api/students/{id}/class-assignments", student.getId())
				.header("Authorization", bearer(teacherToken)))
			.andExpect(status().isOk());
		mockMvc.perform(post("/api/students/{id}/class-assignments/leave", student.getId())
				.header("Authorization", bearer(teacherToken)).contentType(MediaType.APPLICATION_JSON)
				.content("{\"effectiveDate\":\"2027-01-01\"}"))
			.andExpect(status().isForbidden());

		String studentToken = tokenForRole(UserRole.STUDENT, student);
		mockMvc.perform(get("/api/students/{id}/class-assignments", student.getId())
				.header("Authorization", bearer(studentToken)))
			.andExpect(status().isOk());
	}

	@Test
	void rejectsNewAssignmentThatOverlapsClosedHistory() throws Exception {
		Student student = studentRepository.save(new Student("ASSIGN-OVERLAP-" + shortId(), "Assignment Overlap", null,
				null, null, null, null));
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass firstClass = schoolClassRepository.save(new SchoolClass("Overlap First " + shortId(), 2026, teacher));
		SchoolClass secondClass = schoolClassRepository
			.save(new SchoolClass("Overlap Second " + shortId(), 2026, teacher));
		String adminToken = adminToken();

		mockMvc.perform(post("/api/students/{id}/class-assignments", student.getId())
				.header("Authorization", bearer(adminToken)).contentType(MediaType.APPLICATION_JSON)
				.content("{\"classId\":%d,\"effectiveDate\":\"2026-09-01\"}".formatted(firstClass.getId())))
			.andExpect(status().isOk());
		mockMvc.perform(post("/api/students/{id}/class-assignments/leave", student.getId())
				.header("Authorization", bearer(adminToken)).contentType(MediaType.APPLICATION_JSON)
				.content("{\"effectiveDate\":\"2027-01-01\"}"))
			.andExpect(status().isOk());

		mockMvc.perform(post("/api/students/{id}/class-assignments", student.getId())
				.header("Authorization", bearer(adminToken)).contentType(MediaType.APPLICATION_JSON)
				.content("{\"classId\":%d,\"effectiveDate\":\"2026-12-01\"}".formatted(secondClass.getId())))
			.andExpect(status().isBadRequest());
		mockMvc.perform(get("/api/students/{id}/class-assignments", student.getId())
				.header("Authorization", bearer(adminToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$", hasSize(1)));
	}

	@Test
	void serializesConcurrentClassAssignmentsForSameStudent() throws Exception {
		Student student = studentRepository.save(new Student("ASSIGN-CONCURRENT-" + shortId(), "Concurrent Assignment",
				null, null, null, null, null));
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass firstClass = schoolClassRepository
			.save(new SchoolClass("Concurrent First " + shortId(), 2026, teacher));
		SchoolClass secondClass = schoolClassRepository
			.save(new SchoolClass("Concurrent Second " + shortId(), 2026, teacher));
		var executor = Executors.newFixedThreadPool(2);
		try {
			List<Future<Boolean>> futures = List.of(
					executor.submit(() -> tryAssign(student.getId(), firstClass.getId())),
					executor.submit(() -> tryAssign(student.getId(), secondClass.getId())));
			long successfulAssignments = futures.stream().filter(future -> {
				try {
					return future.get();
				}
				catch (Exception exception) {
					throw new RuntimeException(exception);
				}
			}).count();
			assertEquals(1, successfulAssignments);
			assertEquals(1, studentClassAssignmentRepository.findByStudentIdOrderByStartDateDesc(student.getId()).size());
		}
		finally {
			executor.shutdownNow();
		}
	}

	@Test
	void adminCreatesCourseAndTeacherSearchesIt() throws Exception {
		String courseName = "Database Principles " + shortId();
		String request = """
				{"name":"%s","credits":3.5}
				""".formatted(courseName);

		mockMvc.perform(post("/api/courses").header("Authorization", bearer(adminToken()))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isCreated())
			.andExpect(header().string("Location", matchesPattern(".*/api/courses/\\d+")))
			.andExpect(jsonPath("$.courseCode", matchesPattern("C\\d{8}")))
			.andExpect(jsonPath("$.name", is(courseName)))
			.andExpect(jsonPath("$.credits", is(3.5)))
			.andExpect(jsonPath("$.status", is("ACTIVE")));

		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		String teacherToken = authService.login(new LoginRequest(teacher.getUsername(), "StrongPassword123!")).token();
		mockMvc.perform(get("/api/courses").header("Authorization", bearer(teacherToken))
				.param("keyword", courseName).param("status", "ACTIVE"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].name", is(courseName)));
	}

	@Test
	void rejectsDuplicateCourseAndInvalidCredits() throws Exception {
		String courseName = "Duplicate Course " + shortId();
		String request = """
				{"name":"%s","credits":2.0,"status":"INACTIVE"}
				""".formatted(courseName);
		String adminToken = adminToken();

		mockMvc.perform(post("/api/courses").header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isCreated());
		mockMvc.perform(post("/api/courses").header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isConflict());
		mockMvc.perform(post("/api/courses").header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Invalid Credits\",\"credits\":0.1}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void onlyStaffCanAccessCoursesAndOnlyAdminCanCreate() throws Exception {
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		String teacherToken = authService.login(new LoginRequest(teacher.getUsername(), "StrongPassword123!")).token();
		String request = "{\"name\":\"Denied Course %s\",\"credits\":1.0}".formatted(shortId());

		mockMvc.perform(post("/api/courses").header("Authorization", bearer(teacherToken))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isForbidden());

		Student student = studentRepository.save(new Student("COURSE-STUDENT-" + shortId(), "Course Student", null, null,
				null, null, null));
		String studentToken = tokenForRole(UserRole.STUDENT, student);
		mockMvc.perform(get("/api/courses").header("Authorization", bearer(studentToken)))
			.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/courses"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void adminCreatesCourseOfferingAndTeacherSearchesIt() throws Exception {
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass schoolClass = schoolClassRepository.save(new SchoolClass("Offering Class " + shortId(), 2026, teacher));
		Course course = saveCourse(CourseStatus.ACTIVE);
		String request = """
				{"courseId":%d,"classId":%d,"teacherId":%d,"academicYear":2026,"semester":"FIRST"}
				""".formatted(course.getId(), schoolClass.getId(), teacher.getId());

		mockMvc.perform(post("/api/course-offerings").header("Authorization", bearer(adminToken()))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.courseId", is(course.getId().intValue())))
			.andExpect(jsonPath("$.classId", is(schoolClass.getId().intValue())))
			.andExpect(jsonPath("$.teacherId", is(teacher.getId().intValue())))
			.andExpect(jsonPath("$.academicYear", is(2026)))
			.andExpect(jsonPath("$.semester", is("FIRST")));

		String teacherToken = authService.login(new LoginRequest(teacher.getUsername(), "StrongPassword123!")).token();
		mockMvc.perform(get("/api/course-offerings").header("Authorization", bearer(teacherToken))
				.param("classId", schoolClass.getId().toString()).param("academicYear", "2026")
				.param("semester", "FIRST"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].courseCode", is(course.getCourseCode())));
	}

	@Test
	void rejectsDuplicateOfferingInactiveCourseAndInvalidTeacher() throws Exception {
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass schoolClass = schoolClassRepository.save(new SchoolClass("Offering Rules " + shortId(), 2026, teacher));
		Course activeCourse = saveCourse(CourseStatus.ACTIVE);
		String adminToken = adminToken();
		String request = """
				{"courseId":%d,"classId":%d,"teacherId":%d,"academicYear":2027,"semester":"SECOND"}
				""".formatted(activeCourse.getId(), schoolClass.getId(), teacher.getId());

		mockMvc.perform(post("/api/course-offerings").header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isCreated());
		mockMvc.perform(post("/api/course-offerings").header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isConflict());

		Course inactiveCourse = saveCourse(CourseStatus.INACTIVE);
		String inactiveRequest = """
				{"courseId":%d,"classId":%d,"teacherId":%d,"academicYear":2027,"semester":"FIRST"}
				""".formatted(inactiveCourse.getId(), schoolClass.getId(), teacher.getId());
		mockMvc.perform(post("/api/course-offerings").header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON).content(inactiveRequest))
			.andExpect(status().isBadRequest());

		UserAccount invalidTeacher = saveAccount(UserRole.ADMIN, null);
		String invalidTeacherRequest = """
				{"courseId":%d,"classId":%d,"teacherId":%d,"academicYear":2028,"semester":"FIRST"}
				""".formatted(activeCourse.getId(), schoolClass.getId(), invalidTeacher.getId());
		mockMvc.perform(post("/api/course-offerings").header("Authorization", bearer(adminToken))
				.contentType(MediaType.APPLICATION_JSON).content(invalidTeacherRequest))
			.andExpect(status().isBadRequest());
	}

	@Test
	void onlyStaffCanAccessCourseOfferingsAndOnlyAdminCanCreate() throws Exception {
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass schoolClass = schoolClassRepository.save(new SchoolClass("Offering Denied " + shortId(), 2026, teacher));
		Course course = saveCourse(CourseStatus.ACTIVE);
		String request = """
				{"courseId":%d,"classId":%d,"teacherId":%d,"academicYear":2026,"semester":"FIRST"}
				""".formatted(course.getId(), schoolClass.getId(), teacher.getId());
		String teacherToken = authService.login(new LoginRequest(teacher.getUsername(), "StrongPassword123!")).token();

		mockMvc.perform(post("/api/course-offerings").header("Authorization", bearer(teacherToken))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isForbidden());

		Student student = studentRepository.save(new Student("OFFERING-STUDENT-" + shortId(), "Offering Student", null,
				null, null, null, null));
		String studentToken = tokenForRole(UserRole.STUDENT, student);
		mockMvc.perform(get("/api/course-offerings").header("Authorization", bearer(studentToken)))
			.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/course-offerings"))
			.andExpect(status().isUnauthorized());
	}

	@Test
	void studentViewsOwnTimetableAcrossClassesFromTransferHistory() throws Exception {
		Student student = studentRepository.save(new Student("TIMETABLE-" + shortId(), "Timetable Student", null, null,
				null, null, LocalDate.of(2026, 9, 1)));
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass firstClass = schoolClassRepository.save(new SchoolClass("Timetable First " + shortId(), 2026, teacher));
		SchoolClass secondClass = schoolClassRepository
			.save(new SchoolClass("Timetable Second " + shortId(), 2026, teacher));
		StudentClassAssignment firstAssignment = new StudentClassAssignment(student, firstClass, LocalDate.of(2026, 9, 1));
		firstAssignment.close(LocalDate.of(2026, 11, 30));
		studentClassAssignmentRepository.save(firstAssignment);
		studentClassAssignmentRepository
			.save(new StudentClassAssignment(student, secondClass, LocalDate.of(2026, 12, 1)));

		Course firstCourse = saveCourse(CourseStatus.ACTIVE);
		Course secondCourse = saveCourse(CourseStatus.ACTIVE);
		Course otherSemesterCourse = saveCourse(CourseStatus.ACTIVE);
		courseOfferingRepository.save(new CourseOffering(firstCourse, firstClass, teacher, 2026, Semester.FIRST));
		courseOfferingRepository.save(new CourseOffering(secondCourse, secondClass, teacher, 2026, Semester.FIRST));
		courseOfferingRepository
			.save(new CourseOffering(otherSemesterCourse, secondClass, teacher, 2026, Semester.SECOND));

		String studentToken = tokenForRole(UserRole.STUDENT, student);
		mockMvc.perform(get("/api/students/{id}/timetable", student.getId())
				.header("Authorization", bearer(studentToken)).param("academicYear", "2026").param("semester", "FIRST"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.studentId", is(student.getId().intValue())))
			.andExpect(jsonPath("$.classIds", hasSize(2)))
			.andExpect(jsonPath("$.offerings", hasSize(2)))
			.andExpect(jsonPath("$.offerings[0].semester", is("FIRST")))
			.andExpect(jsonPath("$.offerings[1].semester", is("FIRST")));
	}

	@Test
	void staffCanViewTimetableButStudentCannotViewAnotherStudent() throws Exception {
		Student student = studentRepository.save(new Student("TIMETABLE-EMPTY-" + shortId(), "Empty Timetable", null,
				null, null, null, null));
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		String teacherToken = authService.login(new LoginRequest(teacher.getUsername(), "StrongPassword123!")).token();

		mockMvc.perform(get("/api/students/{id}/timetable", student.getId())
				.header("Authorization", bearer(teacherToken)).param("academicYear", "2026").param("semester", "FIRST"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.classIds", hasSize(0)))
			.andExpect(jsonPath("$.offerings", hasSize(0)));

		Student anotherStudent = studentRepository.save(new Student("TIMETABLE-OTHER-" + shortId(), "Other Student",
				null, null, null, null, null));
		String studentToken = tokenForRole(UserRole.STUDENT, anotherStudent);
		mockMvc.perform(get("/api/students/{id}/timetable", student.getId())
				.header("Authorization", bearer(studentToken)).param("academicYear", "2026").param("semester", "FIRST"))
			.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/students/{id}/timetable", student.getId())
				.header("Authorization", bearer(teacherToken)).param("academicYear", "1800").param("semester", "FIRST"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void offeringTeacherCreatesUpdatesGradeAndStudentViewsIt() throws Exception {
		Student student = studentRepository.save(new Student("GRADE-" + shortId(), "Grade Student", null, null, null,
				null, LocalDate.of(2026, 9, 1)));
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass schoolClass = schoolClassRepository.save(new SchoolClass("Grade Class " + shortId(), 2026, teacher));
		studentClassAssignmentRepository
			.save(new StudentClassAssignment(student, schoolClass, LocalDate.of(2026, 9, 1)));
		CourseOffering offering = courseOfferingRepository
			.save(new CourseOffering(saveCourse(CourseStatus.ACTIVE), schoolClass, teacher, 2026, Semester.FIRST));
		String teacherToken = authService.login(new LoginRequest(teacher.getUsername(), "StrongPassword123!")).token();
		String request = """
				{"studentId":%d,"courseOfferingId":%d,"score":88.5}
				""".formatted(student.getId(), offering.getId());

		mockMvc.perform(put("/api/grades").header("Authorization", bearer(teacherToken))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.studentId", is(student.getId().intValue())))
			.andExpect(jsonPath("$.courseOfferingId", is(offering.getId().intValue())))
			.andExpect(jsonPath("$.score", is(88.5)));
		mockMvc.perform(put("/api/grades").header("Authorization", bearer(teacherToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"studentId\":%d,\"courseOfferingId\":%d,\"score\":92.0}".formatted(student.getId(),
						offering.getId())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.score", is(92.0)));

		String studentToken = tokenForRole(UserRole.STUDENT, student);
		mockMvc.perform(get("/api/grades").header("Authorization", bearer(studentToken))
				.param("academicYear", "2026").param("semester", "FIRST"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content", hasSize(1)))
			.andExpect(jsonPath("$.content[0].score", is(92.0)));
	}

	@Test
	void serializesConcurrentFirstGradeUpserts() throws Exception {
		Student student = studentRepository.save(new Student("GRADE-CONCURRENT-" + shortId(), "Concurrent Grade", null,
				null, null, null, LocalDate.of(2026, 9, 1)));
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass schoolClass = schoolClassRepository
			.save(new SchoolClass("Concurrent Grade Class " + shortId(), 2026, teacher));
		studentClassAssignmentRepository
			.save(new StudentClassAssignment(student, schoolClass, LocalDate.of(2026, 9, 1)));
		CourseOffering offering = courseOfferingRepository
			.save(new CourseOffering(saveCourse(CourseStatus.ACTIVE), schoolClass, teacher, 2026, Semester.FIRST));
		AuthenticatedUser user = new AuthenticatedUser(teacher.getId(), teacher.getUsername(), UserRole.TEACHER, null);

		try (var executor = Executors.newFixedThreadPool(2)) {
			List<Future<GradeUpsertResult>> futures = List.of(
					executor.submit(() -> gradeService
						.upsert(new UpsertGradeRequest(student.getId(), offering.getId(), new BigDecimal("80.0")), user)),
					executor.submit(() -> gradeService
						.upsert(new UpsertGradeRequest(student.getId(), offering.getId(), new BigDecimal("90.0")), user)));
			List<GradeUpsertResult> results = futures.stream().map(this::getFuture).toList();
			assertEquals(1, results.stream().filter(GradeUpsertResult::created).count());
			assertEquals(1, gradeRepository.findByStudentIdAndCourseOfferingId(student.getId(), offering.getId()).stream()
				.count());
		}
	}

	@Test
	void enforcesGradeEnrollmentScoreAndRoleRules() throws Exception {
		Student student = studentRepository.save(new Student("GRADE-RULE-" + shortId(), "Grade Rules", null, null, null,
				null, null));
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		UserAccount otherTeacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass schoolClass = schoolClassRepository
			.save(new SchoolClass("Grade Rules Class " + shortId(), 2026, teacher));
		CourseOffering offering = courseOfferingRepository
			.save(new CourseOffering(saveCourse(CourseStatus.ACTIVE), schoolClass, teacher, 2026, Semester.FIRST));
		String teacherToken = authService.login(new LoginRequest(teacher.getUsername(), "StrongPassword123!")).token();
		String otherTeacherToken = authService
			.login(new LoginRequest(otherTeacher.getUsername(), "StrongPassword123!"))
			.token();
		String request = """
				{"studentId":%d,"courseOfferingId":%d,"score":80.0}
				""".formatted(student.getId(), offering.getId());

		mockMvc.perform(put("/api/grades").header("Authorization", bearer(teacherToken))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isBadRequest());

		studentClassAssignmentRepository
			.save(new StudentClassAssignment(student, schoolClass, LocalDate.of(2026, 9, 1)));
		mockMvc.perform(put("/api/grades").header("Authorization", bearer(otherTeacherToken))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isForbidden());
		mockMvc.perform(put("/api/grades").header("Authorization", bearer(teacherToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"studentId\":%d,\"courseOfferingId\":%d,\"score\":101.0}".formatted(student.getId(),
						offering.getId())))
			.andExpect(status().isBadRequest());

		String studentToken = tokenForRole(UserRole.STUDENT, student);
		mockMvc.perform(put("/api/grades").header("Authorization", bearer(studentToken))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isForbidden());
	}

	@Test
	void calculatesCourseOfferingGradeStatisticsAndEnforcesPermissions() throws Exception {
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		UserAccount otherTeacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass schoolClass = schoolClassRepository
			.save(new SchoolClass("Statistics Class " + shortId(), 2026, teacher));
		CourseOffering offering = courseOfferingRepository
			.save(new CourseOffering(saveCourse(CourseStatus.ACTIVE), schoolClass, teacher, 2026, Semester.FIRST));
		Student first = studentRepository
			.save(new Student("STAT-1-" + shortId(), "Statistics One", null, null, null, null, null));
		Student second = studentRepository
			.save(new Student("STAT-2-" + shortId(), "Statistics Two", null, null, null, null, null));
		Student third = studentRepository
			.save(new Student("STAT-3-" + shortId(), "Statistics Three", null, null, null, null, null));
		gradeRepository.saveAll(List.of(new Grade(first, offering, new BigDecimal("80.0")),
				new Grade(second, offering, new BigDecimal("60.0")),
				new Grade(third, offering, new BigDecimal("50.0"))));
		String teacherToken = authService.login(new LoginRequest(teacher.getUsername(), "StrongPassword123!")).token();
		String otherTeacherToken = authService
			.login(new LoginRequest(otherTeacher.getUsername(), "StrongPassword123!"))
			.token();

		mockMvc.perform(get("/api/grade-statistics/course-offerings/{id}", offering.getId())
				.header("Authorization", bearer(teacherToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.gradedCount", is(3)))
			.andExpect(jsonPath("$.averageScore", is(63.33)))
			.andExpect(jsonPath("$.highestScore", is(80.0)))
			.andExpect(jsonPath("$.lowestScore", is(50.0)))
			.andExpect(jsonPath("$.passedCount", is(2)))
			.andExpect(jsonPath("$.failedCount", is(1)))
			.andExpect(jsonPath("$.passRate", is(66.67)));
		mockMvc.perform(get("/api/grade-statistics/course-offerings/{id}", offering.getId())
				.header("Authorization", bearer(otherTeacherToken)))
			.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/grade-statistics/course-offerings/{id}", offering.getId())
				.header("Authorization", bearer(tokenForRole(UserRole.STUDENT, first))))
			.andExpect(status().isForbidden());

		CourseOffering emptyOffering = courseOfferingRepository
			.save(new CourseOffering(saveCourse(CourseStatus.ACTIVE), schoolClass, teacher, 2026, Semester.FIRST));
		mockMvc.perform(get("/api/grade-statistics/course-offerings/{id}", emptyOffering.getId())
				.header("Authorization", bearer(teacherToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.gradedCount", is(0)))
			.andExpect(jsonPath("$.averageScore").doesNotExist())
			.andExpect(jsonPath("$.passRate").doesNotExist());
	}

	@Test
	void calculatesStudentTermGradeStatisticsAndEnforcesPermissions() throws Exception {
		Student student = studentRepository
			.save(new Student("TERM-STAT-" + shortId(), "Term Statistics", null, null, null, null, null));
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass schoolClass = schoolClassRepository
			.save(new SchoolClass("Term Statistics Class " + shortId(), 2026, teacher));
		String firstSuffix = shortId();
		Course firstCourse = courseRepository.save(new Course("S1-" + firstSuffix, "Statistics Course " + firstSuffix,
				new BigDecimal("3.0"), CourseStatus.ACTIVE));
		String secondSuffix = shortId();
		Course secondCourse = courseRepository.save(new Course("S2-" + secondSuffix,
				"Statistics Course " + secondSuffix, new BigDecimal("2.0"), CourseStatus.ACTIVE));
		CourseOffering firstOffering = courseOfferingRepository
			.save(new CourseOffering(firstCourse, schoolClass, teacher, 2026, Semester.FIRST));
		CourseOffering secondOffering = courseOfferingRepository
			.save(new CourseOffering(secondCourse, schoolClass, teacher, 2026, Semester.FIRST));
		gradeRepository.saveAll(List.of(new Grade(student, firstOffering, new BigDecimal("80.0")),
				new Grade(student, secondOffering, new BigDecimal("50.0"))));
		String studentToken = tokenForRole(UserRole.STUDENT, student);

		mockMvc.perform(get("/api/grade-statistics/students/{id}", student.getId())
				.header("Authorization", bearer(studentToken)).param("academicYear", "2026").param("semester", "FIRST"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.gradeCount", is(2)))
			.andExpect(jsonPath("$.totalCredits", is(5.0)))
			.andExpect(jsonPath("$.weightedAverageScore", is(68.0)))
			.andExpect(jsonPath("$.passedCourseCount", is(1)))
			.andExpect(jsonPath("$.failedCourseCount", is(1)));
		mockMvc.perform(get("/api/grade-statistics/students/{id}", student.getId())
				.header("Authorization", bearer(tokenForRole(UserRole.TEACHER, null))).param("academicYear", "2026")
				.param("semester", "FIRST"))
			.andExpect(status().isForbidden());
		Student otherStudent = studentRepository
			.save(new Student("TERM-STAT-OTHER-" + shortId(), "Other Term Statistics", null, null, null, null, null));
		mockMvc.perform(get("/api/grade-statistics/students/{id}", student.getId())
				.header("Authorization", bearer(tokenForRole(UserRole.STUDENT, otherStudent))).param("academicYear", "2026")
				.param("semester", "FIRST"))
			.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/grade-statistics/students/{id}", student.getId())
				.header("Authorization", bearer(adminToken())).param("academicYear", "2027").param("semester", "FIRST"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.gradeCount", is(0)))
			.andExpect(jsonPath("$.weightedAverageScore").doesNotExist());
	}

	@Test
	void offeringTeacherCreatesSessionRecordsAttendanceAndStudentViewsIt() throws Exception {
		Student student = studentRepository.save(new Student("ATTENDANCE-" + shortId(), "Attendance Student", null, null,
				null, null, LocalDate.of(2026, 9, 1)));
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass schoolClass = schoolClassRepository
			.save(new SchoolClass("Attendance Class " + shortId(), 2026, teacher));
		studentClassAssignmentRepository
			.save(new StudentClassAssignment(student, schoolClass, LocalDate.of(2026, 9, 1)));
		CourseOffering offering = courseOfferingRepository
			.save(new CourseOffering(saveCourse(CourseStatus.ACTIVE), schoolClass, teacher, 2026, Semester.FIRST));
		String teacherToken = authService.login(new LoginRequest(teacher.getUsername(), "StrongPassword123!")).token();

		mockMvc.perform(post("/api/attendance-sessions").header("Authorization", bearer(teacherToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"courseOfferingId\":%d,\"sessionDate\":\"2026-09-10\",\"topic\":\"Introduction\"}"
					.formatted(offering.getId())))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.sessionDate", is("2026-09-10")));
		AttendanceSession session = attendanceSessionRepository.findAll().stream()
			.filter(item -> item.getCourseOffering().getId().equals(offering.getId()))
			.findFirst()
			.orElseThrow();

		String record = "{\"attendanceSessionId\":%d,\"studentId\":%d,\"status\":\"LATE\"}".formatted(session.getId(),
				student.getId());
		mockMvc.perform(put("/api/attendance-records").header("Authorization", bearer(teacherToken))
				.contentType(MediaType.APPLICATION_JSON).content(record))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.status", is("LATE")));
		mockMvc.perform(put("/api/attendance-records").header("Authorization", bearer(teacherToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"attendanceSessionId\":%d,\"studentId\":%d,\"status\":\"PRESENT\"}".formatted(session.getId(),
						student.getId())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.status", is("PRESENT")));

		String studentToken = tokenForRole(UserRole.STUDENT, student);
		mockMvc.perform(get("/api/attendance-records").header("Authorization", bearer(studentToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content", hasSize(1)))
			.andExpect(jsonPath("$.content[0].status", is("PRESENT")));
	}

	@Test
	void serializesConcurrentFirstAttendanceUpserts() throws Exception {
		Student student = studentRepository.save(new Student("ATT-CONCURRENT-" + shortId(), "Concurrent Attendance", null,
				null, null, null, LocalDate.of(2026, 9, 1)));
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass schoolClass = schoolClassRepository
			.save(new SchoolClass("Concurrent Attendance Class " + shortId(), 2026, teacher));
		studentClassAssignmentRepository
			.save(new StudentClassAssignment(student, schoolClass, LocalDate.of(2026, 9, 1)));
		CourseOffering offering = courseOfferingRepository
			.save(new CourseOffering(saveCourse(CourseStatus.ACTIVE), schoolClass, teacher, 2026, Semester.FIRST));
		AttendanceSession session = attendanceSessionRepository
			.save(new AttendanceSession(offering, LocalDate.of(2026, 9, 10), "Concurrent"));
		AuthenticatedUser user = new AuthenticatedUser(teacher.getId(), teacher.getUsername(), UserRole.TEACHER, null);

		try (var executor = Executors.newFixedThreadPool(2)) {
			List<Future<AttendanceUpsertResult>> futures = List.of(
					executor.submit(() -> attendanceService.upsertRecord(
							new UpsertAttendanceRecordRequest(session.getId(), student.getId(), AttendanceStatus.PRESENT),
							user)),
					executor.submit(() -> attendanceService.upsertRecord(
							new UpsertAttendanceRecordRequest(session.getId(), student.getId(), AttendanceStatus.ABSENT),
							user)));
			List<AttendanceUpsertResult> results = futures.stream().map(this::getFuture).toList();
			assertEquals(1, results.stream().filter(AttendanceUpsertResult::created).count());
			assertEquals(1, attendanceRecordRepository.findByAttendanceSessionIdAndStudentId(session.getId(),
					student.getId()).stream().count());
		}
	}

	@Test
	void enforcesAttendanceDateEnrollmentAndTeacherScope() throws Exception {
		Student student = studentRepository.save(new Student("ATTENDANCE-RULE-" + shortId(), "Attendance Rules", null,
				null, null, null, null));
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		UserAccount otherTeacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass schoolClass = schoolClassRepository
			.save(new SchoolClass("Attendance Rules Class " + shortId(), 2026, teacher));
		CourseOffering offering = courseOfferingRepository
			.save(new CourseOffering(saveCourse(CourseStatus.ACTIVE), schoolClass, teacher, 2026, Semester.FIRST));
		String teacherToken = authService.login(new LoginRequest(teacher.getUsername(), "StrongPassword123!")).token();
		String otherTeacherToken = authService
			.login(new LoginRequest(otherTeacher.getUsername(), "StrongPassword123!"))
			.token();

		mockMvc.perform(post("/api/attendance-sessions").header("Authorization", bearer(teacherToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"courseOfferingId\":%d,\"sessionDate\":\"2026-08-31\"}".formatted(offering.getId())))
			.andExpect(status().isBadRequest());
		mockMvc.perform(post("/api/attendance-sessions").header("Authorization", bearer(otherTeacherToken))
				.contentType(MediaType.APPLICATION_JSON)
				.content("{\"courseOfferingId\":%d,\"sessionDate\":\"2026-09-10\"}".formatted(offering.getId())))
			.andExpect(status().isForbidden());

		AttendanceSession session = attendanceSessionRepository
			.save(new AttendanceSession(offering, LocalDate.of(2026, 9, 10), "Rules"));
		String record = "{\"attendanceSessionId\":%d,\"studentId\":%d,\"status\":\"ABSENT\"}".formatted(session.getId(),
				student.getId());
		mockMvc.perform(put("/api/attendance-records").header("Authorization", bearer(teacherToken))
				.contentType(MediaType.APPLICATION_JSON).content(record))
			.andExpect(status().isBadRequest());
		studentClassAssignmentRepository
			.save(new StudentClassAssignment(student, schoolClass, LocalDate.of(2026, 9, 1)));
		mockMvc.perform(put("/api/attendance-records").header("Authorization", bearer(otherTeacherToken))
				.contentType(MediaType.APPLICATION_JSON).content(record))
			.andExpect(status().isForbidden());
	}

	@Test
	void calculatesCourseOfferingAttendanceStatisticsAndEnforcesPermissions() throws Exception {
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		UserAccount otherTeacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass schoolClass = schoolClassRepository
			.save(new SchoolClass("Attendance Statistics Class " + shortId(), 2026, teacher));
		CourseOffering offering = courseOfferingRepository
			.save(new CourseOffering(saveCourse(CourseStatus.ACTIVE), schoolClass, teacher, 2026, Semester.FIRST));
		AttendanceSession firstSession = attendanceSessionRepository
			.save(new AttendanceSession(offering, LocalDate.of(2026, 9, 10), "First"));
		attendanceSessionRepository.save(new AttendanceSession(offering, LocalDate.of(2026, 9, 11), "Second"));
		Student present = studentRepository
			.save(new Student("ATT-STAT-1-" + shortId(), "Present Student", null, null, null, null, null));
		Student late = studentRepository
			.save(new Student("ATT-STAT-2-" + shortId(), "Late Student", null, null, null, null, null));
		Student excused = studentRepository
			.save(new Student("ATT-STAT-3-" + shortId(), "Excused Student", null, null, null, null, null));
		Student absent = studentRepository
			.save(new Student("ATT-STAT-4-" + shortId(), "Absent Student", null, null, null, null, null));
		attendanceRecordRepository.saveAll(List.of(new AttendanceRecord(firstSession, present, AttendanceStatus.PRESENT),
				new AttendanceRecord(firstSession, late, AttendanceStatus.LATE),
				new AttendanceRecord(firstSession, excused, AttendanceStatus.EXCUSED),
				new AttendanceRecord(firstSession, absent, AttendanceStatus.ABSENT)));
		String teacherToken = authService.login(new LoginRequest(teacher.getUsername(), "StrongPassword123!")).token();

		mockMvc.perform(get("/api/attendance-statistics/course-offerings/{id}", offering.getId())
				.header("Authorization", bearer(teacherToken)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sessionCount", is(2)))
			.andExpect(jsonPath("$.recordedCount", is(4)))
			.andExpect(jsonPath("$.presentCount", is(1)))
			.andExpect(jsonPath("$.lateCount", is(1)))
			.andExpect(jsonPath("$.excusedCount", is(1)))
			.andExpect(jsonPath("$.absentCount", is(1)))
			.andExpect(jsonPath("$.attendanceRate", is(50.0)));
		mockMvc.perform(get("/api/attendance-statistics/course-offerings/{id}", offering.getId())
				.header("Authorization", bearer(tokenForRole(UserRole.TEACHER, null))))
			.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/attendance-statistics/course-offerings/{id}", offering.getId())
				.header("Authorization", bearer(tokenForRole(UserRole.STUDENT, present))))
			.andExpect(status().isForbidden());

		CourseOffering emptyOffering = courseOfferingRepository
			.save(new CourseOffering(saveCourse(CourseStatus.ACTIVE), schoolClass, otherTeacher, 2026, Semester.FIRST));
		mockMvc.perform(get("/api/attendance-statistics/course-offerings/{id}", emptyOffering.getId())
				.header("Authorization", bearer(adminToken())))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.sessionCount", is(0)))
			.andExpect(jsonPath("$.recordedCount", is(0)))
			.andExpect(jsonPath("$.attendanceRate").doesNotExist());
	}

	@Test
	void calculatesStudentTermAttendanceStatisticsAndEnforcesPermissions() throws Exception {
		Student student = studentRepository
			.save(new Student("ATT-TERM-" + shortId(), "Attendance Term Student", null, null, null, null, null));
		UserAccount teacher = saveAccount(UserRole.TEACHER, null);
		SchoolClass schoolClass = schoolClassRepository
			.save(new SchoolClass("Attendance Term Class " + shortId(), 2026, teacher));
		CourseOffering offering = courseOfferingRepository
			.save(new CourseOffering(saveCourse(CourseStatus.ACTIVE), schoolClass, teacher, 2026, Semester.FIRST));
		List<AttendanceSession> sessions = List.of(
				attendanceSessionRepository.save(new AttendanceSession(offering, LocalDate.of(2026, 9, 10), null)),
				attendanceSessionRepository.save(new AttendanceSession(offering, LocalDate.of(2026, 9, 11), null)),
				attendanceSessionRepository.save(new AttendanceSession(offering, LocalDate.of(2026, 9, 12), null)),
				attendanceSessionRepository.save(new AttendanceSession(offering, LocalDate.of(2026, 9, 13), null)));
		attendanceRecordRepository.saveAll(List.of(new AttendanceRecord(sessions.get(0), student, AttendanceStatus.PRESENT),
				new AttendanceRecord(sessions.get(1), student, AttendanceStatus.LATE),
				new AttendanceRecord(sessions.get(2), student, AttendanceStatus.EXCUSED),
				new AttendanceRecord(sessions.get(3), student, AttendanceStatus.ABSENT)));
		String studentToken = tokenForRole(UserRole.STUDENT, student);

		mockMvc.perform(get("/api/attendance-statistics/students/{id}", student.getId())
				.header("Authorization", bearer(studentToken)).param("academicYear", "2026").param("semester", "FIRST"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.recordedCount", is(4)))
			.andExpect(jsonPath("$.presentCount", is(1)))
			.andExpect(jsonPath("$.lateCount", is(1)))
			.andExpect(jsonPath("$.excusedCount", is(1)))
			.andExpect(jsonPath("$.absentCount", is(1)))
			.andExpect(jsonPath("$.attendanceRate", is(50.0)));
		mockMvc.perform(get("/api/attendance-statistics/students/{id}", student.getId())
				.header("Authorization", bearer(tokenForRole(UserRole.TEACHER, null))).param("academicYear", "2026")
				.param("semester", "FIRST"))
			.andExpect(status().isForbidden());
		Student otherStudent = studentRepository
			.save(new Student("ATT-TERM-OTHER-" + shortId(), "Other Attendance Term Student", null, null, null, null, null));
		mockMvc.perform(get("/api/attendance-statistics/students/{id}", student.getId())
				.header("Authorization", bearer(tokenForRole(UserRole.STUDENT, otherStudent))).param("academicYear", "2026")
				.param("semester", "FIRST"))
			.andExpect(status().isForbidden());
		mockMvc.perform(get("/api/attendance-statistics/students/{id}", student.getId())
				.header("Authorization", bearer(adminToken())).param("academicYear", "2027").param("semester", "FIRST"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.recordedCount", is(0)))
			.andExpect(jsonPath("$.attendanceRate").doesNotExist());
	}

	private void createStudent(String ignoredStudentNumber, String name) throws Exception {
		String request = """
				{"name":"%s"}
				""".formatted(name);
		mockMvc.perform(post("/api/students").header("Authorization", bearer(adminToken()))
				.contentType(MediaType.APPLICATION_JSON).content(request))
			.andExpect(status().isCreated());
	}

	private String adminToken() {
		return tokenForRole(UserRole.ADMIN, null);
	}

	private String tokenForRole(UserRole role, Student student) {
		UserAccount account = saveAccount(role, student);
		return authService.login(new LoginRequest(account.getUsername(), "StrongPassword123!")).token();
	}

	private UserAccount saveAccount(UserRole role, Student student) {
		String username = role.name().toLowerCase() + "-" + UUID.randomUUID();
		return userAccountRepository.save(new UserAccount(username, passwordEncoder.encode("StrongPassword123!"), role,
				student));
	}

	private Course saveCourse(CourseStatus status) {
		String suffix = shortId();
		return courseRepository.save(new Course("T-" + suffix, "Test Course " + suffix, new BigDecimal("3.0"), status));
	}

	private boolean tryAssign(long studentId, long classId) {
		try {
			studentClassAssignmentService.assign(studentId,
					new AssignStudentClassRequest(classId, LocalDate.of(2026, 9, 1)));
			return true;
		}
		catch (RuntimeException exception) {
			return false;
		}
	}

	private <T> T getFuture(Future<T> future) {
		try {
			return future.get();
		}
		catch (Exception exception) {
			throw new RuntimeException(exception);
		}
	}

	private String bearer(String token) {
		return "Bearer " + token;
	}

	private String shortId() {
		return UUID.randomUUID().toString().substring(0, 12);
	}

}
