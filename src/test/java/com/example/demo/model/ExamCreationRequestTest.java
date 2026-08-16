package com.example.demo.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ExamCreationRequestTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void validRequestHasNoViolations() {
    var request = new ExamCreationRequest(LocalDate.now(), 0.5);
    assertThat(validator.validate(request)).isEmpty();
  }

  @Test
  void zeroCoefficientIsRejected() {
    var request = new ExamCreationRequest(LocalDate.now(), 0.0);
    assertThat(validator.validate(request)).isNotEmpty();
  }

  @Test
  void coefficientAboveOneIsRejected() {
    var request = new ExamCreationRequest(LocalDate.now(), 1.5);
    assertThat(validator.validate(request)).isNotEmpty();
  }

  @Test
  void missingDateExamIsRejected() {
    var request = new ExamCreationRequest(null, 0.5);
    assertThat(validator.validate(request)).isNotEmpty();
  }
}
