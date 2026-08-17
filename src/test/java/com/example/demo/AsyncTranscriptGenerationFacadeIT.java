package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.example.demo.endpoint.event.EventProducer;
import com.example.demo.endpoint.event.model.TranscriptGenerationRequested;
import com.example.demo.file.bucket.BucketComponent;
import com.example.demo.file.hash.FileHash;
import com.example.demo.file.hash.FileHashAlgorithm;
import com.example.demo.mail.Email;
import com.example.demo.mail.Mailer;
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
import com.example.demo.service.event.TranscriptGenerationRequestedService;
import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

class AsyncTranscriptGenerationFacadeIT extends FacadeIT {

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
  @Autowired private TranscriptGenerationRequestedService transcriptGenerationRequestedService;

  @MockBean private EventProducer<TranscriptGenerationRequested> eventProducer;
  @MockBean private BucketComponent bucketComponent;
  @MockBean private Mailer mailer;

  @LocalServerPort private int port;

  @BeforeEach
  void disablePersistentConnectionsToAvoidLocalhostKeepAliveDesync() {
    restTemplate.getRestTemplate().setRequestFactory(new SimpleClientHttpRequestFactory());
  }

  @Test
  void requestingATranscriptProducesAnEventThatTheWorkerTurnsIntoAnUploadedPdfAndAnEmail() {
    var teacher = saveUser("i4.teacher@hei.school", Role.TEACHER, null);
    var student = saveUser("i4.student@hei.school", Role.STUDENT, "STD940001");

    var academicYear = saveAcademicYear(LocalDate.of(2031, 9, 1), LocalDate.of(2032, 7, 1));
    var semester = new Semester();
    semester.setAcademicYear(academicYear);
    semester.setSemesterNumber(1);
    semester.setStartDate(academicYear.getStartDate());
    semester.setEndDate(academicYear.getEndDate());
    semester = semesterRepository.save(semester);

    var course = saveCourse("I4-C1", "Async transcript course", 30);
    var courseTrack = new CourseTrack();
    courseTrack.setCourse(course);
    courseTrack.setTrack(Track.COMMON_CORE);
    courseTrack.setSemester(semester);
    courseTrackRepository.save(courseTrack);

    var exam = new Exam();
    exam.setCourse(course);
    exam.setDateExam(academicYear.getStartDate().plusMonths(1));
    exam.setCoefficient(1.0);
    exam = examRepository.save(exam);

    var grade = new GradeHistory();
    grade.setStudent(student);
    grade.setExam(exam);
    grade.setValue(14.0);
    grade.setRecordedAt(Instant.now());
    grade.setRecordedBy(teacher);
    gradeHistoryRepository.save(grade);

    var enrollment = new StudentEnrollment();
    enrollment.setStudent(student);
    var promotion = new Promotion();
    promotion.setLabel("Promo I4");
    promotion.setExpectedGraduationYear(2032);
    enrollment.setPromotion(promotionRepository.save(promotion));
    enrollment.setAcademicYear(academicYear);
    enrollment.setTrackAtTime(Track.COMMON_CORE);
    enrollment.setRepeating(false);
    studentEnrollmentRepository.save(enrollment);

    when(bucketComponent.upload(any(File.class), any(String.class)))
        .thenReturn(new FileHash(FileHashAlgorithm.SHA256, "deadbeef"));

    var response =
        restTemplate.exchange(
            url("/students/" + student.getId() + "/transcripts/" + academicYear.getId()),
            HttpMethod.POST,
            new HttpEntity<Void>(null, authHeaders(student)),
            Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

    var eventCaptor = ArgumentCaptor.forClass(List.class);
    verify(eventProducer).accept(eventCaptor.capture());
    var producedEvents = eventCaptor.getValue();
    assertThat(producedEvents).hasSize(1);
    var producedEvent = (TranscriptGenerationRequested) producedEvents.get(0);
    assertThat(producedEvent.getStudentId()).isEqualTo(student.getId());
    assertThat(producedEvent.getAcademicYearId()).isEqualTo(academicYear.getId());

    transcriptGenerationRequestedService.accept(producedEvent);

    var bucketKeyCaptor = ArgumentCaptor.forClass(String.class);
    var fileCaptor = ArgumentCaptor.forClass(File.class);
    verify(bucketComponent).upload(fileCaptor.capture(), bucketKeyCaptor.capture());
    assertThat(bucketKeyCaptor.getValue())
        .isEqualTo("transcripts/" + student.getId() + "/" + academicYear.getId() + ".pdf");
    assertThat(fileCaptor.getValue()).exists();
    assertThat(fileCaptor.getValue().length()).isPositive();

    var emailCaptor = ArgumentCaptor.forClass(Email.class);
    verify(mailer).accept(emailCaptor.capture());
    assertThat(emailCaptor.getValue().to().getAddress()).isEqualTo("i4.student@hei.school");
    assertThat(emailCaptor.getValue().attachments()).containsExactly(fileCaptor.getValue());
  }

  private Course saveCourse(String ref, String title, int credits) {
    var course = new Course();
    course.setRef(ref);
    course.setTitle(title);
    course.setCredits(credits);
    return courseRepository.save(course);
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
