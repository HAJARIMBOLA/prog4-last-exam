package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Promotion;
import com.example.demo.domain.Role;
import com.example.demo.domain.StudentEnrollment;
import com.example.demo.domain.Track;
import com.example.demo.domain.User;
import com.example.demo.model.CourseDTO;
import com.example.demo.repository.StudentEnrollmentRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class YearRepetitionServiceTest {

  @Mock private StrictTrackFilterService strictTrackFilterService;
  @Mock private CourseAverageService courseAverageService;
  @Mock private YearAverageService yearAverageService;
  @Mock private StudentEnrollmentRepository studentEnrollmentRepository;

  @Test
  void creditsOkAndAverageOkMeansTheStudentPasses() {
    var result = evaluate(List.of(courseOf(30), courseOf(30)), List.of(15.0, 15.0), 15.0);

    assertThat(result.repeating()).isFalse();
  }

  @Test
  void creditsBelowSixtyForcesRepetitionEvenWithAGoodAverage() {
    var result = evaluate(List.of(courseOf(30), courseOf(30)), List.of(15.0, 4.0), 12.0);

    assertThat(result.repeating()).isTrue();
  }

  @Test
  void averageBelowTenForcesRepetitionEvenWithAllCreditsEarned() {
    var result = evaluate(List.of(courseOf(30), courseOf(30)), List.of(15.0, 15.0), 8.0);

    assertThat(result.repeating()).isTrue();
  }

  @Test
  void bothCreditsAndAverageBelowThresholdForceRepetition() {
    var result = evaluate(List.of(courseOf(30), courseOf(30)), List.of(4.0, 4.0), 4.0);

    assertThat(result.repeating()).isTrue();
  }

  private com.example.demo.model.StudentEnrollmentDTO evaluate(
      List<CourseDTO> courses, List<Double> courseAverages, double yearAverage) {
    var studentId = UUID.randomUUID();
    var academicYearId = UUID.randomUUID();
    var student =
        new User(studentId, "s@hei.school", "hash", Role.STUDENT, "STD001", "Jane", "Doe");
    var promotion = new Promotion(UUID.randomUUID(), "P2026", 2029);
    var academicYear = new AcademicYear(academicYearId, LocalDate.now(), LocalDate.now());
    var enrollment =
        new StudentEnrollment(UUID.randomUUID(), student, promotion, academicYear, Track.EL, false);

    when(strictTrackFilterService.getCoursesForStudentAtYear(studentId, academicYearId))
        .thenReturn(courses);
    for (var i = 0; i < courses.size(); i++) {
      when(courseAverageService.computeCourseAverage(studentId, courses.get(i).id()))
          .thenReturn(Optional.of(courseAverages.get(i)));
    }
    when(yearAverageService.computeYearAverage(studentId, academicYearId))
        .thenReturn(Optional.of(yearAverage));
    when(studentEnrollmentRepository.findByStudentIdAndAcademicYearId(studentId, academicYearId))
        .thenReturn(Optional.of(enrollment));
    when(studentEnrollmentRepository.save(any(StudentEnrollment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var service =
        new YearRepetitionService(
            strictTrackFilterService,
            courseAverageService,
            yearAverageService,
            studentEnrollmentRepository);

    return service.evaluateAndUpdateRepetition(studentId, academicYearId);
  }

  private CourseDTO courseOf(int credits) {
    return new CourseDTO(UUID.randomUUID(), "REF" + UUID.randomUUID(), "Title", credits);
  }
}
