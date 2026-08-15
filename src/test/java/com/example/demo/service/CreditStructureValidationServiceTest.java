package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Course;
import com.example.demo.domain.CourseTrack;
import com.example.demo.domain.Semester;
import com.example.demo.domain.Track;
import com.example.demo.exception.InvalidCreditStructureException;
import com.example.demo.repository.CourseTrackRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreditStructureValidationServiceTest {

  @Mock private CourseTrackRepository courseTrackRepository;

  @Test
  void aThirtyThirtyStructureIsValid() {
    var service = new CreditStructureValidationService(courseTrackRepository);

    assertThatCode(() -> service.validateCompleteYearStructure(30, 30)).doesNotThrowAnyException();
  }

  @Test
  void aTwentyFiveThirtyFiveStructureIsRejectedEvenThoughTheYearTotalIsSixty() {
    var service = new CreditStructureValidationService(courseTrackRepository);

    assertThatThrownBy(() -> service.validateCompleteYearStructure(25, 35))
        .isInstanceOf(InvalidCreditStructureException.class);
  }

  @Test
  void addingACourseThatStaysUnderTheCapIsAccepted() {
    var track = Track.EL;
    var semesterId = UUID.randomUUID();
    when(courseTrackRepository.findByTrackAndSemesterId(track, semesterId))
        .thenReturn(List.of(courseTrackWithCredits(20)));
    var service = new CreditStructureValidationService(courseTrackRepository);

    assertThatCode(() -> service.assertAddingCourseWithinSemesterCap(track, semesterId, 6))
        .doesNotThrowAnyException();
  }

  @Test
  void addingACourseThatWouldExceedTheCapIsRejected() {
    var track = Track.EL;
    var semesterId = UUID.randomUUID();
    when(courseTrackRepository.findByTrackAndSemesterId(track, semesterId))
        .thenReturn(List.of(courseTrackWithCredits(28)));
    var service = new CreditStructureValidationService(courseTrackRepository);

    assertThatThrownBy(() -> service.assertAddingCourseWithinSemesterCap(track, semesterId, 4))
        .isInstanceOf(InvalidCreditStructureException.class);
  }

  private CourseTrack courseTrackWithCredits(int credits) {
    var course = new Course(UUID.randomUUID(), "ALG101", "Algorithms", credits);
    var academicYear = new AcademicYear(UUID.randomUUID(), LocalDate.now(), LocalDate.now());
    var semester =
        new Semester(UUID.randomUUID(), academicYear, 1, LocalDate.now(), LocalDate.now());
    return new CourseTrack(UUID.randomUUID(), course, Track.EL, semester);
  }
}
