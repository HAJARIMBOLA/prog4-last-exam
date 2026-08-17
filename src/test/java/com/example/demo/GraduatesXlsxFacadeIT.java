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
import com.example.demo.file.bucket.BucketComponent;
import com.example.demo.model.GraduatesDownloadResponse;
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
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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

class GraduatesXlsxFacadeIT extends FacadeIT {

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

  @MockBean private BucketComponent bucketComponent;

  @LocalServerPort private int port;

  @BeforeEach
  void disablePersistentConnectionsToAvoidLocalhostKeepAliveDesync() {
    restTemplate.getRestTemplate().setRequestFactory(new SimpleClientHttpRequestFactory());
  }

  @Test
  void downloadsARealXlsxContainingOnlyGraduationEligibleStudentsSortedByRank() throws Exception {
    var teacher = saveUser("i5.teacher@hei.school", Role.TEACHER, null);
    var admin = saveUser("i5.admin@hei.school", Role.ADMIN, null);

    var topStudent = saveUser("i5.top@hei.school", Role.STUDENT, "STD950001");
    topStudent.setFirstName("Toky");
    topStudent.setLastName("Rakoto");
    userRepository.save(topStudent);

    var middleStudent = saveUser("i5.middle@hei.school", Role.STUDENT, "STD950002");
    middleStudent.setFirstName("Hery");
    middleStudent.setLastName("Andria");
    userRepository.save(middleStudent);

    var ineligibleStudent = saveUser("i5.ineligible@hei.school", Role.STUDENT, "STD950003");

    var promotion = new Promotion();
    promotion.setLabel("Promo I5");
    promotion.setExpectedGraduationYear(2044);
    promotion = promotionRepository.save(promotion);

    var year1 = saveAcademicYear(LocalDate.of(2041, 9, 1), LocalDate.of(2042, 7, 1));
    var year2 = saveAcademicYear(LocalDate.of(2042, 9, 1), LocalDate.of(2043, 7, 1));
    var year3 = saveAcademicYear(LocalDate.of(2043, 9, 1), LocalDate.of(2044, 7, 1));

    var exam1 = saveExamWithCourse(year1, "I5-Y1");
    var exam2 = saveExamWithCourse(year2, "I5-Y2");
    var exam3 = saveExamWithCourse(year3, "I5-Y3");

    for (var year : List.of(year1, year2, year3)) {
      enrollStudent(topStudent, promotion, year);
      enrollStudent(middleStudent, promotion, year);
      enrollStudent(ineligibleStudent, promotion, year);
    }

    grade(exam1, topStudent, teacher, 18.0);
    grade(exam2, topStudent, teacher, 18.0);
    grade(exam3, topStudent, teacher, 18.0);

    grade(exam1, middleStudent, teacher, 14.0);
    grade(exam2, middleStudent, teacher, 14.0);
    grade(exam3, middleStudent, teacher, 14.0);

    grade(exam1, ineligibleStudent, teacher, 15.0);
    grade(exam2, ineligibleStudent, teacher, 15.0);
    grade(exam3, ineligibleStudent, teacher, 8.0);

    var presignedUrl = URI.create("https://bucket.example.com/graduates.xlsx").toURL();
    when(bucketComponent.presign(any(String.class), any())).thenReturn(presignedUrl);

    var response =
        restTemplate.exchange(
            url("/promotions/" + promotion.getId() + "/graduates/download?track=EL"),
            HttpMethod.GET,
            new HttpEntity<Void>(null, authHeaders(admin)),
            GraduatesDownloadResponse.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody().downloadUrl()).isEqualTo(presignedUrl.toString());

    var fileCaptor = ArgumentCaptor.forClass(File.class);
    verify(bucketComponent).upload(fileCaptor.capture(), any(String.class));

    try (var inputStream = new FileInputStream(fileCaptor.getValue());
        var workbook = new XSSFWorkbook(inputStream)) {
      var sheet = workbook.getSheetAt(0);
      var header = sheet.getRow(0);
      assertThat(header.getCell(0).getStringCellValue()).isEqualTo("rank");
      assertThat(header.getCell(1).getStringCellValue()).isEqualTo("STD");
      assertThat(header.getCell(2).getStringCellValue()).isEqualTo("last name");
      assertThat(header.getCell(3).getStringCellValue()).isEqualTo("first name");
      assertThat(header.getCell(4).getStringCellValue()).isEqualTo("general average");

      var firstRow = sheet.getRow(1);
      assertThat(firstRow.getCell(0).getNumericCellValue()).isEqualTo(1.0);
      assertThat(firstRow.getCell(1).getStringCellValue()).isEqualTo("STD950001");
      assertThat(firstRow.getCell(2).getStringCellValue()).isEqualTo("Rakoto");
      assertThat(firstRow.getCell(3).getStringCellValue()).isEqualTo("Toky");

      var secondRow = sheet.getRow(2);
      assertThat(secondRow.getCell(0).getNumericCellValue()).isEqualTo(2.0);
      assertThat(secondRow.getCell(1).getStringCellValue()).isEqualTo("STD950002");

      assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(3);
    }
  }

  private void enrollStudent(User student, Promotion promotion, AcademicYear academicYear) {
    var enrollment = new StudentEnrollment();
    enrollment.setStudent(student);
    enrollment.setPromotion(promotion);
    enrollment.setAcademicYear(academicYear);
    enrollment.setTrackAtTime(Track.EL);
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

  private Exam saveExamWithCourse(AcademicYear academicYear, String ref) {
    var semester = new Semester();
    semester.setAcademicYear(academicYear);
    semester.setSemesterNumber(1);
    semester.setStartDate(academicYear.getStartDate());
    semester.setEndDate(academicYear.getEndDate());
    semester = semesterRepository.save(semester);

    var course = new Course();
    course.setRef(ref);
    course.setTitle("Course " + ref);
    course.setCredits(60);
    course = courseRepository.save(course);

    var courseTrack = new CourseTrack();
    courseTrack.setCourse(course);
    courseTrack.setTrack(Track.EL);
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
