package com.example.demo.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class ChangePasswordRequestTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void validRequestHasNoViolations() {
    var request = new ChangePasswordRequest("oldPassword", "newPassword1");
    assertThat(validator.validate(request)).isEmpty();
  }

  @Test
  void blankCurrentPasswordIsRejected() {
    var request = new ChangePasswordRequest(" ", "newPassword1");
    assertThat(validator.validate(request)).isNotEmpty();
  }

  @Test
  void newPasswordShorterThanEightCharactersIsRejected() {
    var request = new ChangePasswordRequest("oldPassword", "short1");
    assertThat(validator.validate(request)).isNotEmpty();
  }
}
