package com.example.demo.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class CreateStudentRequestTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void validRequestHasNoViolations() {
    var request = new CreateStudentRequest("s@hei.school", "Jane", "Doe", "STD001");
    assertThat(validator.validate(request)).isEmpty();
  }

  @Test
  void malformedEmailIsRejected() {
    var request = new CreateStudentRequest("not-an-email", "Jane", "Doe", "STD001");
    assertThat(validator.validate(request)).isNotEmpty();
  }

  @Test
  void blankMatriculationNumberIsRejected() {
    var request = new CreateStudentRequest("s@hei.school", "Jane", "Doe", " ");
    assertThat(validator.validate(request)).isNotEmpty();
  }
}
