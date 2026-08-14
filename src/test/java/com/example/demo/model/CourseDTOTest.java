package com.example.demo.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CourseDTOTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void validCourseHasNoViolations() {
    var course = new CourseDTO(UUID.randomUUID(), "ALG101", "Algorithms", 4);
    assertThat(validator.validate(course)).isEmpty();
  }

  @Test
  void blankRefIsRejected() {
    var course = new CourseDTO(UUID.randomUUID(), " ", "Algorithms", 4);
    assertThat(validator.validate(course)).isNotEmpty();
  }

  @Test
  void nonPositiveCreditsIsRejected() {
    var course = new CourseDTO(UUID.randomUUID(), "ALG101", "Algorithms", 0);
    assertThat(validator.validate(course)).isNotEmpty();
  }
}
