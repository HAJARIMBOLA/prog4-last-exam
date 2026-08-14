package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Course;
import com.example.demo.domain.Exam;
import com.example.demo.model.CourseDTO;
import com.example.demo.model.GradeHistoryDTO;
import com.example.demo.repository.ExamRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TranscriptSnapshotBuilderTest {

  @Mock private StrictTrackFilterService strictTrackFilterService;
  @Mock private ExamRepository examRepository;
  @Mock private GradeHistoryService gradeHistoryService;

  @Test
  void buildsOneLinePerExamWithItsActiveGradeWhenAvailable() {
    var studentId = UUID.randomUUID();
    var academicYearId = UUID.randomUUID();
    var courseId = UUID.randomUUID();
    var courseDTO = new CourseDTO(courseId, "ALG101", "Algorithms", 4);
    when(strictTrackFilterService.getCoursesForStudentAtYear(studentId, academicYearId))
        .thenReturn(List.of(courseDTO));

    var course = new Course(courseId, "ALG101", "Algorithms", 4);
    var gradedExam = new Exam(UUID.randomUUID(), LocalDate.now(), 0.5, course);
    var ungradedExam = new Exam(UUID.randomUUID(), LocalDate.now(), 0.5, course);
    when(examRepository.findByCourseId(courseId)).thenReturn(List.of(gradedExam, ungradedExam));

    when(gradeHistoryService.getActiveGrade(studentId, gradedExam.getId()))
        .thenReturn(
            Optional.of(
                new GradeHistoryDTO(
                    UUID.randomUUID(),
                    studentId,
                    gradedExam.getId(),
                    14.0,
                    Instant.now(),
                    UUID.randomUUID())));
    when(gradeHistoryService.getActiveGrade(studentId, ungradedExam.getId()))
        .thenReturn(Optional.empty());

    var builder =
        new TranscriptSnapshotBuilder(
            strictTrackFilterService, examRepository, gradeHistoryService);
    var snapshot = builder.build(studentId, academicYearId);

    assertThat(snapshot.lines()).hasSize(2);
    assertThat(snapshot.lines())
        .filteredOn(line -> line.examId().equals(gradedExam.getId()))
        .extracting("grade")
        .containsExactly(14.0);
    assertThat(snapshot.lines())
        .filteredOn(line -> line.examId().equals(ungradedExam.getId()))
        .extracting("grade")
        .containsExactly((Object) null);
  }
}
