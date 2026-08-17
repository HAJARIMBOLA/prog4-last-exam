package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Course;
import com.example.demo.domain.CourseAssignment;
import com.example.demo.domain.Exam;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.model.CreateStudentRequest;
import com.example.demo.model.ExamCreationRequest;
import com.example.demo.model.GradeHistoryDTO;
import com.example.demo.model.GradeSubmissionRequest;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.CourseAssignmentRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

class SecurityFacadeIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JwtService jwtService;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private UserRepository userRepository;
  @Autowired private AcademicYearRepository academicYearRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private CourseAssignmentRepository courseAssignmentRepository;
  @Autowired private ExamRepository examRepository;

  @LocalServerPort private int port;

  @BeforeEach
  void disablePersistentConnectionsToAvoidLocalhostKeepAliveDesync() {
    restTemplate.getRestTemplate().setRequestFactory(new SimpleClientHttpRequestFactory());
  }

  @Test
  void unauthenticatedRequestToAProtectedEndpointIsRejectedWith401() {
    var response = restTemplate.getForEntity(url("/courses"), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void studentCannotReadAnotherStudentsGrades() {
    var owner = saveUser("i2.owner@hei.school", Role.STUDENT, "STD920001");
    var intruder = saveUser("i2.intruder@hei.school", Role.STUDENT, "STD920002");

    var response =
        restTemplate.exchange(
            url("/students/" + owner.getId() + "/grades"),
            HttpMethod.GET,
            new HttpEntity<Void>(null, authHeaders(intruder)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void studentCannotCreateAnotherStudentAccount() {
    var student = saveUser("i2.wannabe-admin@hei.school", Role.STUDENT, "STD920003");
    var request = new CreateStudentRequest("i2.new@hei.school", "New", "Student", "STD920004");

    var response =
        restTemplate.exchange(
            url("/admin/students"),
            HttpMethod.POST,
            new HttpEntity<>(request, authHeaders(student)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void adminCanCreateAStudentAccount() {
    var admin = saveUser("i2.admin@hei.school", Role.ADMIN, null);
    var request =
        new CreateStudentRequest("i2.created@hei.school", "Created", "Student", "STD920005");

    var response =
        restTemplate.exchange(
            url("/admin/students"),
            HttpMethod.POST,
            new HttpEntity<>(request, authHeaders(admin)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(userRepository.findByEmail("i2.created@hei.school")).isPresent();
  }

  @Test
  void unassignedTeacherCannotGradeAnExamForACourseTheyAreNotAssignedTo() {
    var unassignedTeacher = saveUser("i2.unassigned@hei.school", Role.TEACHER, null);
    var assignedTeacher = saveUser("i2.assigned@hei.school", Role.TEACHER, null);
    var student = saveUser("i2.gradee@hei.school", Role.STUDENT, "STD920006");

    var academicYear = saveAcademicYear(LocalDate.of(2011, 9, 1), LocalDate.of(2012, 7, 1));
    var course = saveCourse("I2-C1", "Security course");
    var exam = saveExam(course, academicYear.getStartDate().plusMonths(1));

    var assignment = new CourseAssignment();
    assignment.setCourse(course);
    assignment.setTeacher(assignedTeacher);
    assignment.setAcademicYear(academicYear);
    courseAssignmentRepository.save(assignment);

    var request = new GradeSubmissionRequest(student.getId(), 15.0);

    var response =
        restTemplate.exchange(
            url("/exams/" + exam.getId() + "/grades"),
            HttpMethod.POST,
            new HttpEntity<>(request, authHeaders(unassignedTeacher)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void assignedTeacherCanGradeTheirOwnExam() {
    var teacher = saveUser("i2.teacher@hei.school", Role.TEACHER, null);
    var student = saveUser("i2.student2@hei.school", Role.STUDENT, "STD920007");

    var academicYear = saveAcademicYear(LocalDate.of(2012, 9, 1), LocalDate.of(2013, 7, 1));
    var course = saveCourse("I2-C2", "Networking course");
    var exam = saveExam(course, academicYear.getStartDate().plusMonths(1));

    var assignment = new CourseAssignment();
    assignment.setCourse(course);
    assignment.setTeacher(teacher);
    assignment.setAcademicYear(academicYear);
    courseAssignmentRepository.save(assignment);

    var request = new GradeSubmissionRequest(student.getId(), 15.0);

    var response =
        restTemplate.exchange(
            url("/exams/" + exam.getId() + "/grades"),
            HttpMethod.POST,
            new HttpEntity<>(request, authHeaders(teacher)),
            GradeHistoryDTO.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(response.getBody().value()).isEqualTo(15.0);
  }

  @Test
  void examCreationRequiresAdminEvenForATeacherWithAValidToken() {
    var teacher = saveUser("i2.exam-teacher@hei.school", Role.TEACHER, null);
    var course = saveCourse("I2-C3", "Exam creation restricted course");
    var request = new ExamCreationRequest(LocalDate.of(2012, 10, 1), 0.5);

    var response =
        restTemplate.exchange(
            url("/courses/" + course.getId() + "/exams"),
            HttpMethod.POST,
            new HttpEntity<>(request, authHeaders(teacher)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  private Course saveCourse(String ref, String title) {
    var course = new Course();
    course.setRef(ref);
    course.setTitle(title);
    course.setCredits(6);
    return courseRepository.save(course);
  }

  private Exam saveExam(Course course, LocalDate dateExam) {
    var exam = new Exam();
    exam.setCourse(course);
    exam.setDateExam(dateExam);
    exam.setCoefficient(1.0);
    return examRepository.save(exam);
  }

  private AcademicYear saveAcademicYear(LocalDate start, LocalDate end) {
    var academicYear = new AcademicYear();
    academicYear.setStartDate(start);
    academicYear.setEndDate(end);
    return academicYearRepository.save(academicYear);
  }

  private User saveUser(String email, Role role, String matriculationNumber) {
    var user = new User();
    user.setEmail(email);
    user.setPasswordHash(passwordEncoder.encode("Password123!"));
    user.setRole(role);
    user.setMatriculationNumber(matriculationNumber);
    user.setFirstName("First");
    user.setLastName("Last");
    return userRepository.save(user);
  }

  private HttpHeaders authHeaders(User user) {
    var headers = new HttpHeaders();
    headers.setBearerAuth(jwtService.generateToken(user));
    return headers;
  }

  private String url(String path) {
    return "http://localhost:" + port + path;
  }
}
