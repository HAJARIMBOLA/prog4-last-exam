package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Course;
import com.example.demo.domain.Exam;
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
class CourseAverageServiceTest {

  @Mock private ExamRepository examRepository;
  @Mock private GradeHistoryService gradeHistoryService;

  @Test
  void fullyGradedCourseComputesTheWeightedSum() {
    var studentId = UUID.randomUUID();
    var courseId = UUID.randomUUID();
    var course = new Course(courseId, "ALG101", "Algorithms", 4);
    var exam1 = new Exam(UUID.randomUUID(), LocalDate.now(), 0.6, course);
    var exam2 = new Exam(UUID.randomUUID(), LocalDate.now(), 0.4, course);
    when(examRepository.findByCourseId(courseId)).thenReturn(List.of(exam1, exam2));
    when(gradeHistoryService.getActiveGrade(studentId, exam1.getId()))
        .thenReturn(Optional.of(gradeOf(studentId, exam1.getId(), 15.0)));
    when(gradeHistoryService.getActiveGrade(studentId, exam2.getId()))
        .thenReturn(Optional.of(gradeOf(studentId, exam2.getId(), 10.0)));

    var service = new CourseAverageService(examRepository, gradeHistoryService);

    assertThat(service.computeCourseAverage(studentId, courseId)).contains(13.0);
  }

  @Test
  void partiallyGradedCourseOnlyCountsGradedExams() {
    var studentId = UUID.randomUUID();
    var courseId = UUID.randomUUID();
    var course = new Course(courseId, "ALG101", "Algorithms", 4);
    var exam1 = new Exam(UUID.randomUUID(), LocalDate.now(), 0.6, course);
    var exam2 = new Exam(UUID.randomUUID(), LocalDate.now(), 0.4, course);
    when(examRepository.findByCourseId(courseId)).thenReturn(List.of(exam1, exam2));
    when(gradeHistoryService.getActiveGrade(studentId, exam1.getId()))
        .thenReturn(Optional.of(gradeOf(studentId, exam1.getId(), 15.0)));
    when(gradeHistoryService.getActiveGrade(studentId, exam2.getId())).thenReturn(Optional.empty());

    var service = new CourseAverageService(examRepository, gradeHistoryService);

    assertThat(service.computeCourseAverage(studentId, courseId)).contains(9.0);
  }

  @Test
  void ungradedCourseReturnsEmpty() {
    var studentId = UUID.randomUUID();
    var courseId = UUID.randomUUID();
    var course = new Course(courseId, "ALG101", "Algorithms", 4);
    var exam1 = new Exam(UUID.randomUUID(), LocalDate.now(), 1.0, course);
    when(examRepository.findByCourseId(courseId)).thenReturn(List.of(exam1));
    when(gradeHistoryService.getActiveGrade(studentId, exam1.getId())).thenReturn(Optional.empty());

    var service = new CourseAverageService(examRepository, gradeHistoryService);

    assertThat(service.computeCourseAverage(studentId, courseId)).isEmpty();
  }

  private GradeHistoryDTO gradeOf(UUID studentId, UUID examId, double value) {
    return new GradeHistoryDTO(
        UUID.randomUUID(), studentId, examId, value, Instant.now(), UUID.randomUUID());
  }
}
