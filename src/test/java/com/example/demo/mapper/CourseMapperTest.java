package com.example.demo.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Course;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CourseMapperTest {

  @Test
  void mapsEntityToDTO() {
    var course = new Course(UUID.randomUUID(), "ALG101", "Algorithms", 4);
    var dto = CourseMapper.toDTO(course);

    assertThat(dto.ref()).isEqualTo("ALG101");
    assertThat(dto.title()).isEqualTo("Algorithms");
    assertThat(dto.credits()).isEqualTo(4);
  }
}
