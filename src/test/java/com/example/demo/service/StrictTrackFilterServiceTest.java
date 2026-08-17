package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Course;
import com.example.demo.domain.CourseTrack;
import com.example.demo.domain.Semester;
import com.example.demo.domain.Track;
import com.example.demo.repository.CourseTrackRepository;
import com.example.demo.repository.SemesterRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StrictTrackFilterServiceTest {

  @Mock private StudentEnrollmentService studentEnrollmentService;
  @Mock private SemesterRepository semesterRepository;
  @Mock private CourseTrackRepository courseTrackRepository;

  @Test
  void neverLeaksAWrongTrackCourseAcrossACommonCoreElTnElSequence() {
    var studentId = UUID.randomUUID();
    var commonCoreCourse = new Course(UUID.randomUUID(), "COM101", "Shared Intro", 4);
    var elCourseYear2 = new Course(UUID.randomUUID(), "EL101", "EL Specialty", 4);
    var tnCourse = new Course(UUID.randomUUID(), "TN101", "TN Specialty", 4);

    var year1 = UUID.randomUUID();
    var year2 = UUID.randomUUID();
    var year3 = UUID.randomUUID();
    var year4 = UUID.randomUUID();
    var semesterYear1 = semesterOf(year1);
    var semesterYear2 = semesterOf(year2);
    var semesterYear3 = semesterOf(year3);
    var semesterYear4 = semesterOf(year4);

    when(semesterRepository.findByAcademicYearId(year1)).thenReturn(List.of(semesterYear1));
    when(semesterRepository.findByAcademicYearId(year2)).thenReturn(List.of(semesterYear2));
    when(semesterRepository.findByAcademicYearId(year3)).thenReturn(List.of(semesterYear3));
    when(semesterRepository.findByAcademicYearId(year4)).thenReturn(List.of(semesterYear4));

    when(courseTrackRepository.findByTrackAndSemesterId(Track.COMMON_CORE, semesterYear1.getId()))
        .thenReturn(List.of(courseTrackOf(commonCoreCourse, Track.COMMON_CORE, semesterYear1)));
    when(courseTrackRepository.findByTrackAndSemesterId(Track.EL, semesterYear2.getId()))
        .thenReturn(List.of(courseTrackOf(elCourseYear2, Track.EL, semesterYear2)));
    when(courseTrackRepository.findByTrackAndSemesterId(Track.TN, semesterYear3.getId()))
        .thenReturn(List.of(courseTrackOf(tnCourse, Track.TN, semesterYear3)));
    when(courseTrackRepository.findByTrackAndSemesterId(Track.EL, semesterYear4.getId()))
        .thenReturn(List.of());

    var service =
        new StrictTrackFilterService(
            studentEnrollmentService, semesterRepository, courseTrackRepository);

    when(studentEnrollmentService.resolveTrack(studentId, year1)).thenReturn(Track.COMMON_CORE);
    when(studentEnrollmentService.resolveTrack(studentId, year2)).thenReturn(Track.EL);
    when(studentEnrollmentService.resolveTrack(studentId, year3)).thenReturn(Track.TN);
    when(studentEnrollmentService.resolveTrack(studentId, year4)).thenReturn(Track.EL);

    assertThat(service.getCoursesForStudentAtYear(studentId, year1))
        .extracting("ref")
        .containsExactly("COM101");
    assertThat(service.getCoursesForStudentAtYear(studentId, year2))
        .extracting("ref")
        .containsExactly("EL101");
    assertThat(service.getCoursesForStudentAtYear(studentId, year3))
        .extracting("ref")
        .containsExactly("TN101");
    assertThat(service.getCoursesForStudentAtYear(studentId, year4)).isEmpty();
  }

  @Test
  void doesNotLeakAnEarlierYearsCourseOfTheSameTrackIntoALaterYear() {
    var studentId = UUID.randomUUID();
    var elCourseYear2 = new Course(UUID.randomUUID(), "EL101", "EL Specialty Year 2", 4);
    var elCourseYear3 = new Course(UUID.randomUUID(), "EL201", "EL Specialty Year 3", 4);

    var year2 = UUID.randomUUID();
    var year3 = UUID.randomUUID();
    var semesterYear2 = semesterOf(year2);
    var semesterYear3 = semesterOf(year3);

    when(semesterRepository.findByAcademicYearId(year2)).thenReturn(List.of(semesterYear2));
    when(semesterRepository.findByAcademicYearId(year3)).thenReturn(List.of(semesterYear3));
    when(courseTrackRepository.findByTrackAndSemesterId(Track.EL, semesterYear2.getId()))
        .thenReturn(List.of(courseTrackOf(elCourseYear2, Track.EL, semesterYear2)));
    when(courseTrackRepository.findByTrackAndSemesterId(Track.EL, semesterYear3.getId()))
        .thenReturn(List.of(courseTrackOf(elCourseYear3, Track.EL, semesterYear3)));
    when(studentEnrollmentService.resolveTrack(studentId, year2)).thenReturn(Track.EL);
    when(studentEnrollmentService.resolveTrack(studentId, year3)).thenReturn(Track.EL);

    var service =
        new StrictTrackFilterService(
            studentEnrollmentService, semesterRepository, courseTrackRepository);

    assertThat(service.getCoursesForStudentAtYear(studentId, year2))
        .extracting("ref")
        .containsExactly("EL101");
    assertThat(service.getCoursesForStudentAtYear(studentId, year3))
        .extracting("ref")
        .containsExactly("EL201");
  }

  private Semester semesterOf(UUID academicYearId) {
    var academicYear = new AcademicYear(academicYearId, LocalDate.now(), LocalDate.now());
    var semester = new Semester();
    semester.setId(UUID.randomUUID());
    semester.setAcademicYear(academicYear);
    semester.setSemesterNumber(1);
    semester.setStartDate(LocalDate.now());
    semester.setEndDate(LocalDate.now());
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
