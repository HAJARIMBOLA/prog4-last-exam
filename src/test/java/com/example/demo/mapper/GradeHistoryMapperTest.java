package com.example.demo.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Course;
import com.example.demo.domain.Exam;
import com.example.demo.domain.GradeHistory;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GradeHistoryMapperTest {

  @Test
  void mapsEntityToDTO() {
    var student =
        new User(UUID.randomUUID(), "s@hei.school", "hash", Role.STUDENT, "STD001", "Jane", "Doe");
    var teacher =
        new User(UUID.randomUUID(), "t@hei.school", "hash", Role.TEACHER, null, "John", "Smith");
    var course = new Course(UUID.randomUUID(), "ALG101", "Algorithms", 4);
    var exam = new Exam(UUID.randomUUID(), LocalDate.now(), 0.5, course);
    var gradeHistory =
        new GradeHistory(UUID.randomUUID(), student, exam, 15.0, Instant.now(), teacher);

    var dto = GradeHistoryMapper.toDTO(gradeHistory);

    assertThat(dto.studentId()).isEqualTo(student.getId());
    assertThat(dto.examId()).isEqualTo(exam.getId());
    assertThat(dto.value()).isEqualTo(15.0);
    assertThat(dto.recordedById()).isEqualTo(teacher.getId());
  }
}
