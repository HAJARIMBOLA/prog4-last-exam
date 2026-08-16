package com.example.demo.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class LoginRequestTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void validRequestHasNoViolations() {
    var request = new LoginRequest("s@hei.school", "password");
    assertThat(validator.validate(request)).isEmpty();
  }

  @Test
  void malformedEmailIsRejected() {
    var request = new LoginRequest("not-an-email", "password");
    assertThat(validator.validate(request)).isNotEmpty();
  }

  @Test
  void blankPasswordIsRejected() {
    var request = new LoginRequest("s@hei.school", " ");
    assertThat(validator.validate(request)).isNotEmpty();
  }
}
