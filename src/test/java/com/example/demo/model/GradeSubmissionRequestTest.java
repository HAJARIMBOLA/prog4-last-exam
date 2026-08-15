package com.example.demo.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GradeSubmissionRequestTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void validGradeHasNoViolations() {
    var request = new GradeSubmissionRequest(UUID.randomUUID(), 15.5);
    assertThat(validator.validate(request)).isEmpty();
  }

  @Test
  void boundaryValuesAreAccepted() {
    assertThat(validator.validate(new GradeSubmissionRequest(UUID.randomUUID(), 0.0))).isEmpty();
    assertThat(validator.validate(new GradeSubmissionRequest(UUID.randomUUID(), 20.0))).isEmpty();
  }

  @Test
  void negativeValueIsRejected() {
    var request = new GradeSubmissionRequest(UUID.randomUUID(), -1.0);
    assertThat(validator.validate(request)).isNotEmpty();
  }

  @Test
  void valueAboveTwentyIsRejected() {
    var request = new GradeSubmissionRequest(UUID.randomUUID(), 20.5);
    assertThat(validator.validate(request)).isNotEmpty();
  }

  @Test
  void missingValueIsRejected() {
    var request = new GradeSubmissionRequest(UUID.randomUUID(), null);
    assertThat(validator.validate(request)).isNotEmpty();
  }
}
