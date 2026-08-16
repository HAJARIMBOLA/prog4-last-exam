package com.example.demo.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChangeGroupRequestTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void validRequestHasNoViolations() {
    var request = new ChangeGroupRequest(UUID.randomUUID(), LocalDate.now());
    assertThat(validator.validate(request)).isEmpty();
  }

  @Test
  void missingGroupIdIsRejected() {
    var request = new ChangeGroupRequest(null, LocalDate.now());
    assertThat(validator.validate(request)).isNotEmpty();
  }

  @Test
  void missingEffectiveDateIsRejected() {
    var request = new ChangeGroupRequest(UUID.randomUUID(), null);
    assertThat(validator.validate(request)).isNotEmpty();
  }
}
