package com.example.demo.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Course;
import com.example.demo.domain.Exam;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExamMapperTest {

  @Test
  void mapsEntityToDTOIncludingCourseId() {
    var course = new Course(UUID.randomUUID(), "ALG101", "Algorithms", 4);
    var exam = new Exam(UUID.randomUUID(), LocalDate.of(2026, 6, 1), 0.5, course);

    var dto = ExamMapper.toDTO(exam);

    assertThat(dto.coefficient()).isEqualTo(0.5);
    assertThat(dto.courseId()).isEqualTo(course.getId());
  }
}
