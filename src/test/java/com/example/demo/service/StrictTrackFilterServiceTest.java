package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Course;
import com.example.demo.domain.Track;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StrictTrackFilterServiceTest {

  @Mock private StudentEnrollmentService studentEnrollmentService;
  @Mock private com.example.demo.repository.CourseRepository courseRepository;
  @Mock private CourseTrackValidationService courseTrackValidationService;

  @Test
  void neverLeaksAWrongTrackCourseAcrossACommonCoreElTnElSequence() {
    var studentId = UUID.randomUUID();
    var commonCoreCourse = new Course(UUID.randomUUID(), "COM101", "Shared Intro", 4);
    var elCourse = new Course(UUID.randomUUID(), "EL101", "EL Specialty", 4);
    var tnCourse = new Course(UUID.randomUUID(), "TN101", "TN Specialty", 4);
    when(courseRepository.findAll()).thenReturn(List.of(commonCoreCourse, elCourse, tnCourse));

    when(courseTrackValidationService.courseMatchesTrack(eq(commonCoreCourse.getId()), any()))
        .thenAnswer(invocation -> invocation.getArgument(1) == Track.COMMON_CORE);
    when(courseTrackValidationService.courseMatchesTrack(eq(elCourse.getId()), any()))
        .thenAnswer(invocation -> invocation.getArgument(1) == Track.EL);
    when(courseTrackValidationService.courseMatchesTrack(eq(tnCourse.getId()), any()))
        .thenAnswer(invocation -> invocation.getArgument(1) == Track.TN);

    var service =
        new StrictTrackFilterService(
            studentEnrollmentService, courseRepository, courseTrackValidationService);

    var year1 = UUID.randomUUID();
    var year2 = UUID.randomUUID();
    var year3 = UUID.randomUUID();
    var year4 = UUID.randomUUID();
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
    assertThat(service.getCoursesForStudentAtYear(studentId, year4))
        .extracting("ref")
        .containsExactly("EL101");
  }
}
