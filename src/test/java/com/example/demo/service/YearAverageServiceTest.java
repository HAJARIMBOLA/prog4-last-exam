package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Course;
import com.example.demo.domain.Track;
import com.example.demo.model.CourseDTO;
import com.example.demo.repository.CourseRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class YearAverageServiceTest {

  @Mock private StudentEnrollmentService studentEnrollmentService;
  @Mock private CourseRepository courseRepository;
  @Mock private CourseTrackValidationService courseTrackValidationService;
  @Mock private StrictTrackFilterService strictTrackFilterService;
  @Mock private CourseAverageService courseAverageService;

  @Test
  void computesTheCreditWeightedAverageAcrossGradedCourses() {
    var studentId = UUID.randomUUID();
    var academicYearId = UUID.randomUUID();
    var course1 = new CourseDTO(UUID.randomUUID(), "ALG101", "Algorithms", 6);
    var course2 = new CourseDTO(UUID.randomUUID(), "DB101", "Databases", 4);
    when(strictTrackFilterService.getCoursesForStudentAtYear(studentId, academicYearId))
        .thenReturn(List.of(course1, course2));
    when(courseAverageService.computeCourseAverage(studentId, course1.id()))
        .thenReturn(Optional.of(15.0));
    when(courseAverageService.computeCourseAverage(studentId, course2.id()))
        .thenReturn(Optional.of(10.0));

    var service = new YearAverageService(strictTrackFilterService, courseAverageService);

    assertThat(service.computeYearAverage(studentId, academicYearId)).contains(13.0);
  }

  @Test
  void trackChangeMidPathOnlyAveragesTheStudentsRealTrackCoursesForThatYear() {
    var studentId = UUID.randomUUID();
    var commonCoreCourse = new Course(UUID.randomUUID(), "COM101", "Shared Intro", 30);
    var elCourse = new Course(UUID.randomUUID(), "EL101", "EL Specialty", 30);
    var tnCourse = new Course(UUID.randomUUID(), "TN101", "TN Specialty", 30);
    when(courseRepository.findAll()).thenReturn(List.of(commonCoreCourse, elCourse, tnCourse));
    when(courseTrackValidationService.courseMatchesTrack(eq(commonCoreCourse.getId()), any()))
        .thenAnswer(invocation -> invocation.getArgument(1) == Track.COMMON_CORE);
    when(courseTrackValidationService.courseMatchesTrack(eq(elCourse.getId()), any()))
        .thenAnswer(invocation -> invocation.getArgument(1) == Track.EL);
    when(courseTrackValidationService.courseMatchesTrack(eq(tnCourse.getId()), any()))
        .thenAnswer(invocation -> invocation.getArgument(1) == Track.TN);

    var realStrictTrackFilterService =
        new StrictTrackFilterService(
            studentEnrollmentService, courseRepository, courseTrackValidationService);
    var service = new YearAverageService(realStrictTrackFilterService, courseAverageService);

    var year1 = UUID.randomUUID();
    var year2 = UUID.randomUUID();
    when(studentEnrollmentService.resolveTrack(studentId, year1)).thenReturn(Track.COMMON_CORE);
    when(studentEnrollmentService.resolveTrack(studentId, year2)).thenReturn(Track.EL);
    when(courseAverageService.computeCourseAverage(studentId, commonCoreCourse.getId()))
        .thenReturn(Optional.of(12.0));
    when(courseAverageService.computeCourseAverage(studentId, elCourse.getId()))
        .thenReturn(Optional.of(14.0));

    assertThat(service.computeYearAverage(studentId, year1)).contains(12.0);
    assertThat(service.computeYearAverage(studentId, year2)).contains(14.0);
    verify(courseAverageService, never()).computeCourseAverage(studentId, tnCourse.getId());
  }

  @Test
  void noGradedCoursesReturnsEmpty() {
    var studentId = UUID.randomUUID();
    var academicYearId = UUID.randomUUID();
    var course = new CourseDTO(UUID.randomUUID(), "ALG101", "Algorithms", 6);
    when(strictTrackFilterService.getCoursesForStudentAtYear(studentId, academicYearId))
        .thenReturn(List.of(course));
    when(courseAverageService.computeCourseAverage(studentId, course.id()))
        .thenReturn(Optional.empty());

    var service = new YearAverageService(strictTrackFilterService, courseAverageService);

    assertThat(service.computeYearAverage(studentId, academicYearId)).isEmpty();
  }
}
