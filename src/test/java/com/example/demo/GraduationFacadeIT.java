package com.example.demo;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.conf.FacadeIT;
import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Course;
import com.example.demo.domain.CourseTrack;
import com.example.demo.domain.Exam;
import com.example.demo.domain.GradeHistory;
import com.example.demo.domain.Group;
import com.example.demo.domain.GroupMembership;
import com.example.demo.domain.Promotion;
import com.example.demo.domain.Role;
import com.example.demo.domain.Semester;
import com.example.demo.domain.StudentEnrollment;
import com.example.demo.domain.Track;
import com.example.demo.domain.User;
import com.example.demo.model.ChangeGroupRequest;
import com.example.demo.model.GroupMembershipDTO;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.CourseTrackRepository;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.GradeHistoryRepository;
import com.example.demo.repository.GroupMembershipRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.SemesterRepository;
import com.example.demo.repository.StudentEnrollmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import com.example.demo.service.GraduationService;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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

class GraduationFacadeIT extends FacadeIT {

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
  @Autowired private GroupRepository groupRepository;
  @Autowired private GroupMembershipRepository groupMembershipRepository;
  @Autowired private GraduationService graduationService;

  @LocalServerPort private int port;

  @BeforeEach
  void disablePersistentConnectionsToAvoidLocalhostKeepAliveDesync() {
    restTemplate.getRestTemplate().setRequestFactory(new SimpleClientHttpRequestFactory());
  }

  @Test
  void graduatesAStudentWhoChangedGroupMidPathAndSwitchedFromCommonCoreToEl() {
    var teacher = saveUser("i1.teacher@hei.school", Role.TEACHER, null);
    var admin = saveUser("i1.admin@hei.school", Role.ADMIN, null);
    var student = saveUser("i1.student@hei.school", Role.STUDENT, "STDI1001");

    var promotion = new Promotion();
    promotion.setLabel("Promo I1");
    promotion.setExpectedGraduationYear(2004);
    promotion = promotionRepository.save(promotion);

    var year1 = saveAcademicYear(LocalDate.of(2001, 9, 1), LocalDate.of(2002, 7, 1));
    var year2 = saveAcademicYear(LocalDate.of(2002, 9, 1), LocalDate.of(2003, 7, 1));
    var year3 = saveAcademicYear(LocalDate.of(2003, 9, 1), LocalDate.of(2004, 7, 1));

    gradeYear(year1, Track.COMMON_CORE, student, teacher, "I1-CC");
    gradeYear(year2, Track.EL, student, teacher, "I1-EL2");
    gradeYear(year3, Track.EL, student, teacher, "I1-EL3");

    saveEnrollment(student, promotion, year1, Track.COMMON_CORE, false);
    saveEnrollment(student, promotion, year2, Track.EL, false);
    saveEnrollment(student, promotion, year3, Track.EL, false);

    var groupK1 = saveGroup("K1-I1");
    var groupK3 = saveGroup("K3-I1");

    var initialMembership = new GroupMembership();
    initialMembership.setStudent(student);
    initialMembership.setGroup(groupK1);
    initialMembership.setDateStart(year1.getStartDate());
    initialMembership.setDateEnd(null);
    groupMembershipRepository.save(initialMembership);

    var adminHeaders = authHeaders(admin);
    var changeGroupRequest = new ChangeGroupRequest(groupK3.getId(), year2.getStartDate());
    var changeGroupResponse =
        restTemplate.exchange(
            url("/students/" + student.getId() + "/group-memberships"),
            HttpMethod.POST,
            new HttpEntity<>(changeGroupRequest, adminHeaders),
            GroupMembershipDTO.class);
    assertThat(changeGroupResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    var historyResponse =
        restTemplate.exchange(
            url("/students/" + student.getId() + "/group-memberships"),
            HttpMethod.GET,
            new HttpEntity<>(adminHeaders),
            GroupMembershipDTO[].class);
    var history = List.of(historyResponse.getBody());
    assertThat(history).hasSize(2);
    assertThat(history.get(0).groupRef()).isEqualTo("K1-I1");
    assertThat(history.get(0).dateEnd()).isEqualTo(year2.getStartDate().minusDays(1));
    assertThat(history.get(1).groupRef()).isEqualTo("K3-I1");
    assertThat(history.get(1).dateEnd()).isNull();

    assertThat(graduationService.isEligibleForGraduation(student.getId())).isTrue();
  }

  private void gradeYear(
      AcademicYear academicYear, Track track, User student, User teacher, String refPrefix) {
    var semester = new Semester();
    semester.setAcademicYear(academicYear);
    semester.setSemesterNumber(1);
    semester.setStartDate(academicYear.getStartDate());
    semester.setEndDate(academicYear.getEndDate());
    semester = semesterRepository.save(semester);

    for (var i = 1; i <= 2; i++) {
      var course = new Course();
      course.setRef(refPrefix + "-C" + i);
      course.setTitle("Course " + refPrefix + " " + i);
      course.setCredits(6);
      course = courseRepository.save(course);

      var courseTrack = new CourseTrack();
      courseTrack.setCourse(course);
      courseTrack.setTrack(track);
      courseTrack.setSemester(semester);
      courseTrackRepository.save(courseTrack);

      var exam = new Exam();
      exam.setDateExam(academicYear.getStartDate().plusMonths(2));
      exam.setCoefficient(1.0);
      exam.setCourse(course);
      exam = examRepository.save(exam);

      var grade = new GradeHistory();
      grade.setStudent(student);
      grade.setExam(exam);
      grade.setValue(14.0);
      grade.setRecordedAt(Instant.now());
      grade.setRecordedBy(teacher);
      gradeHistoryRepository.save(grade);
    }
  }

  private void saveEnrollment(
      User student,
      Promotion promotion,
      AcademicYear academicYear,
      Track track,
      boolean repeating) {
    var enrollment = new StudentEnrollment();
    enrollment.setStudent(student);
    enrollment.setPromotion(promotion);
    enrollment.setAcademicYear(academicYear);
    enrollment.setTrackAtTime(track);
    enrollment.setRepeating(repeating);
    studentEnrollmentRepository.save(enrollment);
  }

  private AcademicYear saveAcademicYear(LocalDate start, LocalDate end) {
    var academicYear = new AcademicYear();
    academicYear.setStartDate(start);
    academicYear.setEndDate(end);
    return academicYearRepository.save(academicYear);
  }

  private Group saveGroup(String ref) {
    var group = new Group();
    group.setRef(ref);
    return groupRepository.save(group);
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
