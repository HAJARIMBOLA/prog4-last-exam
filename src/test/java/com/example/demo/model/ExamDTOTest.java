package com.example.demo.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExamDTOTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void coefficientWithinRangeHasNoViolations() {
    var exam = new ExamDTO(UUID.randomUUID(), LocalDate.now(), 0.5, UUID.randomUUID());
    assertThat(validator.validate(exam)).isEmpty();
  }

  @Test
  void coefficientOfOneHasNoViolations() {
    var exam = new ExamDTO(UUID.randomUUID(), LocalDate.now(), 1.0, UUID.randomUUID());
    assertThat(validator.validate(exam)).isEmpty();
  }

  @Test
  void zeroCoefficientIsRejected() {
    var exam = new ExamDTO(UUID.randomUUID(), LocalDate.now(), 0.0, UUID.randomUUID());
    assertThat(validator.validate(exam)).isNotEmpty();
  }

  @Test
  void negativeCoefficientIsRejected() {
    var exam = new ExamDTO(UUID.randomUUID(), LocalDate.now(), -0.5, UUID.randomUUID());
    assertThat(validator.validate(exam)).isNotEmpty();
  }

  @Test
  void coefficientAboveOneIsRejected() {
    var exam = new ExamDTO(UUID.randomUUID(), LocalDate.now(), 1.5, UUID.randomUUID());
    assertThat(validator.validate(exam)).isNotEmpty();
  }
}
