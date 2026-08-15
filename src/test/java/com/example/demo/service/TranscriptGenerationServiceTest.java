package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.demo.model.CourseDTO;
import com.example.demo.model.TranscriptSnapshotDTO;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranscriptGenerationServiceTest {

  @Mock private TranscriptSnapshotBuilder transcriptSnapshotBuilder;
  @Mock private StrictTrackFilterService strictTrackFilterService;
  @Mock private ExamCreationService examCreationService;
  @Mock private YearAverageService yearAverageService;

  @Test
  void oneIncompleteCourseMarksTheTranscriptAsProvisional() {
    var studentId = UUID.randomUUID();
    var academicYearId = UUID.randomUUID();
    var completeCourse = new CourseDTO(UUID.randomUUID(), "ALG101", "Algorithms", 6);
    var incompleteCourse = new CourseDTO(UUID.randomUUID(), "DB101", "Databases", 4);
    when(strictTrackFilterService.getCoursesForStudentAtYear(studentId, academicYearId))
        .thenReturn(List.of(completeCourse, incompleteCourse));
    when(examCreationService.isCourseComplete(completeCourse.id())).thenReturn(true);
    when(examCreationService.isCourseComplete(incompleteCourse.id())).thenReturn(false);
    when(transcriptSnapshotBuilder.build(studentId, academicYearId))
        .thenReturn(new TranscriptSnapshotDTO(studentId, academicYearId, Instant.now(), List.of()));
    when(yearAverageService.computeYearAverage(studentId, academicYearId))
        .thenReturn(Optional.of(12.0));

    var service =
        new TranscriptGenerationService(
            transcriptSnapshotBuilder,
            strictTrackFilterService,
            examCreationService,
            yearAverageService);
    var transcript = service.generate(studentId, academicYearId);

    assertThat(transcript.provisional()).isTrue();
    assertThat(transcript.totalCredits()).isEqualTo(10);
  }

  @Test
  void allCoursesCompleteMarksTheTranscriptAsCompleteWithGeneralAverage() {
    var studentId = UUID.randomUUID();
    var academicYearId = UUID.randomUUID();
    var course1 = new CourseDTO(UUID.randomUUID(), "ALG101", "Algorithms", 6);
    var course2 = new CourseDTO(UUID.randomUUID(), "DB101", "Databases", 4);
    when(strictTrackFilterService.getCoursesForStudentAtYear(studentId, academicYearId))
        .thenReturn(List.of(course1, course2));
    when(examCreationService.isCourseComplete(course1.id())).thenReturn(true);
    when(examCreationService.isCourseComplete(course2.id())).thenReturn(true);
    when(transcriptSnapshotBuilder.build(studentId, academicYearId))
        .thenReturn(new TranscriptSnapshotDTO(studentId, academicYearId, Instant.now(), List.of()));
    when(yearAverageService.computeYearAverage(studentId, academicYearId))
        .thenReturn(Optional.of(14.5));

    var service =
        new TranscriptGenerationService(
            transcriptSnapshotBuilder,
            strictTrackFilterService,
            examCreationService,
            yearAverageService);
    var transcript = service.generate(studentId, academicYearId);

    assertThat(transcript.provisional()).isFalse();
    assertThat(transcript.generalAverage()).isEqualTo(14.5);
    assertThat(transcript.totalCredits()).isEqualTo(10);
  }
}
