package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Course;
import com.example.demo.domain.CourseTrack;
import com.example.demo.domain.Exam;
import com.example.demo.domain.GradeHistory;
import com.example.demo.domain.Promotion;
import com.example.demo.domain.Role;
import com.example.demo.domain.Semester;
import com.example.demo.domain.StudentEnrollment;
import com.example.demo.domain.Track;
import com.example.demo.domain.User;
import com.example.demo.model.AcademicYearClosingResponse;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.CourseTrackRepository;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.GradeHistoryRepository;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.SemesterRepository;
import com.example.demo.repository.StudentEnrollmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import java.time.Instant;
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

class AcademicYearClosingFacadeIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private JwtService jwtService;
  @Autowired private PasswordEncoder passwordEncoder;
  @Autowired private UserRepository userRepository;
  @Autowired private PromotionRepository promotionRepository;
  @Autowired private AcademicYearRepository academicYearRepository;
  @Autowired private SemesterRepository semesterRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private CourseTrackRepository courseTrackRepository;
  @Autowired private ExamRepository examRepository;
  @Autowired private GradeHistoryRepository gradeHistoryRepository;
  @Autowired private StudentEnrollmentRepository studentEnrollmentRepository;

  @LocalServerPort private int port;

  @BeforeEach
  void disablePersistentConnectionsToAvoidLocalhostKeepAliveDesync() {
    restTemplate.getRestTemplate().setRequestFactory(new SimpleClientHttpRequestFactory());
  }

  @Test
  void closingAnAcademicYearFlagsFailingStudentsAsRepeatingAndLeavesPassingStudentsAlone() {
    var teacher = saveUser("i6.teacher@hei.school", Role.TEACHER, null);
    var admin = saveUser("i6.admin@hei.school", Role.ADMIN, null);
    var passingStudent = saveUser("i6.passing@hei.school", Role.STUDENT, "STD960001");
    var failingStudent = saveUser("i6.failing@hei.school", Role.STUDENT, "STD960002");

    var promotion = new Promotion();
    promotion.setLabel("Promo I6");
    promotion.setExpectedGraduationYear(2052);
    promotion = promotionRepository.save(promotion);

    var academicYear = saveAcademicYear(LocalDate.of(2051, 9, 1), LocalDate.of(2052, 7, 1));
    var semester = new Semester();
    semester.setAcademicYear(academicYear);
    semester.setSemesterNumber(1);
    semester.setStartDate(academicYear.getStartDate());
    semester.setEndDate(academicYear.getEndDate());
    semester = semesterRepository.save(semester);

    var exam1 = saveCourseAndExam("I6-C1", 30, semester, academicYear);
    var exam2 = saveCourseAndExam("I6-C2", 30, semester, academicYear);

    grade(exam1, passingStudent, teacher, 15.0);
    grade(exam2, passingStudent, teacher, 15.0);
    grade(exam1, failingStudent, teacher, 4.0);
    grade(exam2, failingStudent, teacher, 4.0);

    enrollStudent(passingStudent, promotion, academicYear);
    enrollStudent(failingStudent, promotion, academicYear);

    var response =
        restTemplate.exchange(
            url("/academic-years/" + academicYear.getId() + "/close"),
            HttpMethod.POST,
            new HttpEntity<Void>(null, authHeaders(admin)),
            AcademicYearClosingResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().studentsProcessed()).isEqualTo(2);

    var passingEnrollment =
        studentEnrollmentRepository
            .findByStudentIdAndAcademicYearId(passingStudent.getId(), academicYear.getId())
            .orElseThrow();
    assertThat(passingEnrollment.isRepeating()).isFalse();

    var failingEnrollment =
        studentEnrollmentRepository
            .findByStudentIdAndAcademicYearId(failingStudent.getId(), academicYear.getId())
            .orElseThrow();
    assertThat(failingEnrollment.isRepeating()).isTrue();
  }

  private void enrollStudent(User student, Promotion promotion, AcademicYear academicYear) {
    var enrollment = new StudentEnrollment();
    enrollment.setStudent(student);
    enrollment.setPromotion(promotion);
    enrollment.setAcademicYear(academicYear);
    enrollment.setTrackAtTime(Track.COMMON_CORE);
    enrollment.setRepeating(false);
    studentEnrollmentRepository.save(enrollment);
  }

  private void grade(Exam exam, User student, User teacher, double value) {
    var grade = new GradeHistory();
    grade.setStudent(student);
    grade.setExam(exam);
    grade.setValue(value);
    grade.setRecordedAt(Instant.now());
    grade.setRecordedBy(teacher);
    gradeHistoryRepository.save(grade);
  }

  private Exam saveCourseAndExam(
      String ref, int credits, Semester semester, AcademicYear academicYear) {
    var course = new Course();
    course.setRef(ref);
    course.setTitle("Course " + ref);
    course.setCredits(credits);
    course = courseRepository.save(course);

    var courseTrack = new CourseTrack();
    courseTrack.setCourse(course);
    courseTrack.setTrack(Track.COMMON_CORE);
    courseTrack.setSemester(semester);
    courseTrackRepository.save(courseTrack);

    var exam = new Exam();
    exam.setCourse(course);
    exam.setDateExam(academicYear.getStartDate().plusMonths(1));
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
