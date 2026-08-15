package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Course;
import com.example.demo.domain.Exam;
import com.example.demo.domain.GradeHistory;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.GradeHistoryRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GradeSubmissionServiceTest {

  @Mock private GradeHistoryRepository gradeHistoryRepository;
  @Mock private ExamRepository examRepository;
  @Mock private UserRepository userRepository;

  @Test
  void validGradeIsRecordedWithTheAuthenticatedTeacher() {
    var examId = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var course = new Course(UUID.randomUUID(), "ALG101", "Algorithms", 4);
    var exam = new Exam(examId, LocalDate.now(), 0.5, course);
    var student =
        new User(studentId, "s@hei.school", "hash", Role.STUDENT, "STD001", "Jane", "Doe");
    var teacher =
        new User(UUID.randomUUID(), "t@hei.school", "hash", Role.TEACHER, null, "John", "Smith");

    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(userRepository.findByEmail("t@hei.school")).thenReturn(Optional.of(teacher));
    when(gradeHistoryRepository.save(any(GradeHistory.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var service =
        new GradeSubmissionService(gradeHistoryRepository, examRepository, userRepository);
    var result = service.recordGrade(examId, studentId, 15.0, "t@hei.school");

    assertThat(result.value()).isEqualTo(15.0);
    assertThat(result.studentId()).isEqualTo(studentId);
    assertThat(result.recordedById()).isEqualTo(teacher.getId());
  }
}
