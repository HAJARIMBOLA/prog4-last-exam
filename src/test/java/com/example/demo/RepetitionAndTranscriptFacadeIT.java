package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Course;
import com.example.demo.domain.CourseAssignment;
import com.example.demo.domain.CourseTrack;
import com.example.demo.domain.Exam;
import com.example.demo.domain.Promotion;
import com.example.demo.domain.Role;
import com.example.demo.domain.Semester;
import com.example.demo.domain.StudentEnrollment;
import com.example.demo.domain.Track;
import com.example.demo.domain.User;
import com.example.demo.model.GradeHistoryDTO;
import com.example.demo.model.GradeSubmissionRequest;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.CourseAssignmentRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.CourseTrackRepository;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.SemesterRepository;
import com.example.demo.repository.StudentEnrollmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import com.example.demo.service.TranscriptGenerationService;
import com.example.demo.service.YearRepetitionService;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

class RepetitionAndTranscriptFacadeIT extends FacadeIT {

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
  @Autowired private CourseAssignmentRepository courseAssignmentRepository;
  @Autowired private StudentEnrollmentRepository studentEnrollmentRepository;
  @Autowired private YearRepetitionService yearRepetitionService;
  @Autowired private TranscriptGenerationService transcriptGenerationService;

  @LocalServerPort private int port;

  @BeforeEach
  void disablePersistentConnectionsToAvoidLocalhostKeepAliveDesync() {
    restTemplate.getRestTemplate().setRequestFactory(new SimpleClientHttpRequestFactory());
  }

  @Test
  void repeatsTheYearOnLowEarnedCreditsWhileCorrectedGradeReplacesTheClaimedOne() {
    var teacher = saveUser("i3.teacher@hei.school", Role.TEACHER, null);
    var admin = saveUser("i3.admin@hei.school", Role.ADMIN, null);
    var student = saveUser("i3.student@hei.school", Role.STUDENT, "STD930001");

    var promotion = new Promotion();
    promotion.setLabel("Promo I3");
    promotion.setExpectedGraduationYear(2022);
    promotion = promotionRepository.save(promotion);

    var academicYear = saveAcademicYear(LocalDate.of(2021, 9, 1), LocalDate.of(2022, 7, 1));
    var semester = new Semester();
    semester.setAcademicYear(academicYear);
    semester.setSemesterNumber(1);
    semester.setStartDate(academicYear.getStartDate());
    semester.setEndDate(academicYear.getEndDate());
    semester = semesterRepository.save(semester);

    var completeCourse = saveCourse("I3-COMPLETE", "Complete course", 30);
    tagCourseTrack(completeCourse, Track.COMMON_CORE, semester);
    var completeExam = saveExam(completeCourse, academicYear.getStartDate().plusMonths(2), 1.0);
    assignTeacher(completeCourse, teacher, academicYear);

    var incompleteCourse = saveCourse("I3-INCOMPLETE", "Incomplete course", 30);
    tagCourseTrack(incompleteCourse, Track.COMMON_CORE, semester);
    var incompleteExam = saveExam(incompleteCourse, academicYear.getStartDate().plusMonths(2), 0.5);
    assignTeacher(incompleteCourse, teacher, academicYear);

    var enrollment = new StudentEnrollment();
    enrollment.setStudent(student);
    enrollment.setPromotion(promotion);
    enrollment.setAcademicYear(academicYear);
    enrollment.setTrackAtTime(Track.COMMON_CORE);
    enrollment.setRepeating(false);
    studentEnrollmentRepository.save(enrollment);

    var teacherHeaders = authHeaders(teacher);

    var claimedGradeResponse =
        submitGrade(completeExam.getId(), student.getId(), 5.0, teacherHeaders);
    assertThat(claimedGradeResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    var correctedGradeResponse =
        submitGrade(completeExam.getId(), student.getId(), 15.0, teacherHeaders);
    assertThat(correctedGradeResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    submitGrade(incompleteExam.getId(), student.getId(), 12.0, teacherHeaders);

    var historyResponse =
        restTemplate.exchange(
            url("/students/" + student.getId() + "/grades"),
            HttpMethod.GET,
            new HttpEntity<Void>(null, authHeaders(admin)),
            GradeHistoryDTO[].class);
    var allGrades = historyResponse.getBody();
    assertThat(allGrades).hasSize(3);
    var completeCourseGrades =
        Arrays.stream(allGrades)
            .filter(grade -> grade.examId().equals(completeExam.getId()))
            .toList();
    assertThat(completeCourseGrades).hasSize(2);

    var transcript = transcriptGenerationService.generate(student.getId(), academicYear.getId());
    assertThat(transcript.provisional()).isTrue();

    var updatedEnrollment =
        yearRepetitionService.evaluateAndUpdateRepetition(student.getId(), academicYear.getId());
    assertThat(updatedEnrollment.repeating()).isTrue();

    var persistedEnrollment =
        studentEnrollmentRepository
            .findByStudentIdAndAcademicYearId(student.getId(), academicYear.getId())
            .orElseThrow();
    assertThat(persistedEnrollment.isRepeating()).isTrue();
  }

  private ResponseEntity<GradeHistoryDTO> submitGrade(
      UUID examId, UUID studentId, double value, HttpHeaders headers) {
    var request = new GradeSubmissionRequest(studentId, value);
    return restTemplate.exchange(
        url("/exams/" + examId + "/grades"),
        HttpMethod.POST,
        new HttpEntity<>(request, headers),
        GradeHistoryDTO.class);
  }

  private void assignTeacher(Course course, User teacher, AcademicYear academicYear) {
    var assignment = new CourseAssignment();
    assignment.setCourse(course);
    assignment.setTeacher(teacher);
    assignment.setAcademicYear(academicYear);
    courseAssignmentRepository.save(assignment);
  }

  private void tagCourseTrack(Course course, Track track, Semester semester) {
    var courseTrack = new CourseTrack();
    courseTrack.setCourse(course);
    courseTrack.setTrack(track);
    courseTrack.setSemester(semester);
    courseTrackRepository.save(courseTrack);
  }

  private Course saveCourse(String ref, String title, int credits) {
    var course = new Course();
    course.setRef(ref);
    course.setTitle(title);
    course.setCredits(credits);
    return courseRepository.save(course);
  }

  private Exam saveExam(Course course, LocalDate dateExam, double coefficient) {
    var exam = new Exam();
    exam.setCourse(course);
    exam.setDateExam(dateExam);
    exam.setCoefficient(coefficient);
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
