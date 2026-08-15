package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Course;
import com.example.demo.domain.Exam;
import com.example.demo.domain.GradeHistory;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.repository.GradeHistoryRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GradeHistoryServiceTest {

  @Mock private GradeHistoryRepository gradeHistoryRepository;

  @Test
  void activeGradeIsTheMostRecentVersion() {
    var studentId = UUID.randomUUID();
    var examId = UUID.randomUUID();
    var student = studentWith(studentId);
    var teacher = teacherWith(UUID.randomUUID());
    var exam = examWith(examId);

    var oldest =
        new GradeHistory(
            UUID.randomUUID(), student, exam, 8.0, Instant.parse("2026-01-10T10:00:00Z"), teacher);
    var corrected =
        new GradeHistory(
            UUID.randomUUID(), student, exam, 12.0, Instant.parse("2026-01-15T10:00:00Z"), teacher);
    var mostRecent =
        new GradeHistory(
            UUID.randomUUID(), student, exam, 14.0, Instant.parse("2026-01-20T10:00:00Z"), teacher);

    when(gradeHistoryRepository.findByStudentIdAndExamIdOrderByRecordedAtDesc(studentId, examId))
        .thenReturn(List.of(mostRecent, corrected, oldest));

    var service = new GradeHistoryService(gradeHistoryRepository);
    var active = service.getActiveGrade(studentId, examId);

    assertThat(active).isPresent();
    assertThat(active.get().value()).isEqualTo(14.0);
  }

  @Test
  void fullHistoryKeepsEveryVersionInChronologicalOrder() {
    var studentId = UUID.randomUUID();
    var examId = UUID.randomUUID();
    var student = studentWith(studentId);
    var teacher = teacherWith(UUID.randomUUID());
    var exam = examWith(examId);

    var oldest =
        new GradeHistory(
            UUID.randomUUID(), student, exam, 8.0, Instant.parse("2026-01-10T10:00:00Z"), teacher);
    var corrected =
        new GradeHistory(
            UUID.randomUUID(), student, exam, 12.0, Instant.parse("2026-01-15T10:00:00Z"), teacher);
    var mostRecent =
        new GradeHistory(
            UUID.randomUUID(), student, exam, 14.0, Instant.parse("2026-01-20T10:00:00Z"), teacher);

    when(gradeHistoryRepository.findByStudentIdAndExamIdOrderByRecordedAtAsc(studentId, examId))
        .thenReturn(List.of(oldest, corrected, mostRecent));

    var service = new GradeHistoryService(gradeHistoryRepository);
    var history = service.getFullHistory(studentId, examId);

    assertThat(history).extracting("value").containsExactly(8.0, 12.0, 14.0);
  }

  @Test
  void getGradesForStudentReturnsOnlyThatStudentsGrades() {
    var studentAId = UUID.randomUUID();
    var studentA = studentWith(studentAId);
    var teacher = teacherWith(UUID.randomUUID());
    var exam = examWith(UUID.randomUUID());

    var studentAGrade =
        new GradeHistory(UUID.randomUUID(), studentA, exam, 16.0, Instant.now(), teacher);

    when(gradeHistoryRepository.findByStudentId(studentAId)).thenReturn(List.of(studentAGrade));

    var service = new GradeHistoryService(gradeHistoryRepository);
    var grades = service.getGradesForStudent(studentAId);

    assertThat(grades).hasSize(1);
    assertThat(grades.get(0).studentId()).isEqualTo(studentAId);
  }

  private User studentWith(UUID id) {
    return new User(id, "s@hei.school", "hash", Role.STUDENT, "STD001", "Jane", "Doe");
  }

  private User teacherWith(UUID id) {
    return new User(id, "t@hei.school", "hash", Role.TEACHER, null, "John", "Smith");
  }

  private Exam examWith(UUID id) {
    var course = new Course(UUID.randomUUID(), "ALG101", "Algorithms", 4);
    return new Exam(id, LocalDate.now(), 0.5, course);
  }
}
