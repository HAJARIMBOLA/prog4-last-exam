package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Course;
import com.example.demo.domain.CourseTrack;
import com.example.demo.domain.Promotion;
import com.example.demo.domain.Semester;
import com.example.demo.domain.StudentEnrollment;
import com.example.demo.domain.Track;
import com.example.demo.model.CourseDTO;
import com.example.demo.repository.CourseTrackRepository;
import com.example.demo.repository.SemesterRepository;
import com.example.demo.repository.StudentEnrollmentRepository;
import com.example.demo.testdata.AcademicYearTestDataBuilder;
import com.example.demo.testdata.UserTestDataBuilder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraduationServiceTest {

  @Mock private StudentEnrollmentRepository studentEnrollmentRepository;
  @Mock private StrictTrackFilterService strictTrackFilterService;
  @Mock private CourseAverageService courseAverageService;
  @Mock private StudentEnrollmentService studentEnrollmentService;
  @Mock private SemesterRepository semesterRepository;
  @Mock private CourseTrackRepository courseTrackRepository;

  @Test
  void tenOrAboveInEveryCourseOfEveryYearMakesTheStudentEligible() {
    var studentId = UUID.randomUUID();
    var year1 = UUID.randomUUID();
    var year2 = UUID.randomUUID();
    var course1 = new CourseDTO(UUID.randomUUID(), "COM101", "Shared Intro", 30);
    var course2 = new CourseDTO(UUID.randomUUID(), "EL101", "EL Specialty", 30);
    when(studentEnrollmentRepository.findByStudentId(studentId))
        .thenReturn(
            List.of(
                enrollmentOf(studentId, year1, Track.COMMON_CORE, false),
                enrollmentOf(studentId, year2, Track.EL, false)));
    when(strictTrackFilterService.getCoursesForStudentAtYear(studentId, year1))
        .thenReturn(List.of(course1));
    when(strictTrackFilterService.getCoursesForStudentAtYear(studentId, year2))
        .thenReturn(List.of(course2));
    when(courseAverageService.computeCourseAverage(studentId, course1.id()))
        .thenReturn(Optional.of(12.0));
    when(courseAverageService.computeCourseAverage(studentId, course2.id()))
        .thenReturn(Optional.of(11.0));

    var service =
        new GraduationService(
            studentEnrollmentRepository, strictTrackFilterService, courseAverageService);

    assertThat(service.isEligibleForGraduation(studentId)).isTrue();
  }

  @Test
  void oneCourseBelowTenInAnyYearMakesTheStudentNotEligible() {
    var studentId = UUID.randomUUID();
    var year1 = UUID.randomUUID();
    var course1 = new CourseDTO(UUID.randomUUID(), "COM101", "Shared Intro", 30);
    var course2 = new CourseDTO(UUID.randomUUID(), "COM102", "Shared Math", 30);
    when(studentEnrollmentRepository.findByStudentId(studentId))
        .thenReturn(List.of(enrollmentOf(studentId, year1, Track.COMMON_CORE, false)));
    when(strictTrackFilterService.getCoursesForStudentAtYear(studentId, year1))
        .thenReturn(List.of(course1, course2));
    when(courseAverageService.computeCourseAverage(studentId, course1.id()))
        .thenReturn(Optional.of(12.0));
    when(courseAverageService.computeCourseAverage(studentId, course2.id()))
        .thenReturn(Optional.of(9.0));

    var service =
        new GraduationService(
            studentEnrollmentRepository, strictTrackFilterService, courseAverageService);

    assertThat(service.isEligibleForGraduation(studentId)).isFalse();
  }

  @Test
  void aRepeatedYearOnlyChecksTheFinalAttemptAcrossAMidPathTrackAndGroupChange() {
    var studentId = UUID.randomUUID();
    var commonCoreCourse = new Course(UUID.randomUUID(), "COM101", "Shared Intro", 30);
    var elCourse = new Course(UUID.randomUUID(), "EL101", "EL Specialty", 30);

    var year1 = UUID.randomUUID();
    var failedYear2Attempt = UUID.randomUUID();
    var retakenYear2 = UUID.randomUUID();
    var semesterYear1 = semesterOf();
    var semesterRetakenYear2 = semesterOf();
    when(semesterRepository.findByAcademicYearId(year1)).thenReturn(List.of(semesterYear1));
    when(semesterRepository.findByAcademicYearId(retakenYear2))
        .thenReturn(List.of(semesterRetakenYear2));
    when(courseTrackRepository.findByTrackAndSemesterId(Track.COMMON_CORE, semesterYear1.getId()))
        .thenReturn(List.of(courseTrackOf(commonCoreCourse, Track.COMMON_CORE, semesterYear1)));
    when(courseTrackRepository.findByTrackAndSemesterId(Track.EL, semesterRetakenYear2.getId()))
        .thenReturn(List.of(courseTrackOf(elCourse, Track.EL, semesterRetakenYear2)));

    var realStrictTrackFilterService =
        new StrictTrackFilterService(
            studentEnrollmentService, semesterRepository, courseTrackRepository);

    when(studentEnrollmentService.resolveTrack(studentId, year1)).thenReturn(Track.COMMON_CORE);
    when(studentEnrollmentService.resolveTrack(studentId, retakenYear2)).thenReturn(Track.EL);
    when(courseAverageService.computeCourseAverage(studentId, commonCoreCourse.getId()))
        .thenReturn(Optional.of(12.0));
    when(courseAverageService.computeCourseAverage(studentId, elCourse.getId()))
        .thenReturn(Optional.of(11.0));

    when(studentEnrollmentRepository.findByStudentId(studentId))
        .thenReturn(
            List.of(
                enrollmentOf(studentId, year1, Track.COMMON_CORE, false),
                enrollmentOf(studentId, failedYear2Attempt, Track.TN, true),
                enrollmentOf(studentId, retakenYear2, Track.EL, false)));

    var service =
        new GraduationService(
            studentEnrollmentRepository, realStrictTrackFilterService, courseAverageService);

    assertThat(service.isEligibleForGraduation(studentId)).isTrue();
  }

  private StudentEnrollment enrollmentOf(
      UUID studentId, UUID academicYearId, Track track, boolean repeating) {
    var student = UserTestDataBuilder.aStudent().withId(studentId).build();
    var promotion = new Promotion(UUID.randomUUID(), "P2026", 2029);
    var academicYear = AcademicYearTestDataBuilder.anAcademicYear().withId(academicYearId).build();
    return new StudentEnrollment(
        UUID.randomUUID(), student, promotion, academicYear, track, repeating);
  }

  private Semester semesterOf() {
    var semester = new Semester();
    semester.setId(UUID.randomUUID());
    semester.setAcademicYear(AcademicYearTestDataBuilder.anAcademicYear().build());
    semester.setSemesterNumber(1);
    return semester;
  }

  private CourseTrack courseTrackOf(Course course, Track track, Semester semester) {
    var courseTrack = new CourseTrack();
    courseTrack.setId(UUID.randomUUID());
    courseTrack.setCourse(course);
    courseTrack.setTrack(track);
    courseTrack.setSemester(semester);
    return courseTrack;
  }
}
