package com.example.demo.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GroupDTOTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void validRefsHaveNoViolations() {
    assertThat(validator.validate(new GroupDTO(UUID.randomUUID(), "K1"))).isEmpty();
    assertThat(validator.validate(new GroupDTO(UUID.randomUUID(), "K2"))).isEmpty();
    assertThat(validator.validate(new GroupDTO(UUID.randomUUID(), "K3"))).isEmpty();
    assertThat(validator.validate(new GroupDTO(UUID.randomUUID(), "K4"))).isEmpty();
  }

  @Test
  void lowercaseRefIsRejected() {
    assertThat(validator.validate(new GroupDTO(UUID.randomUUID(), "k1"))).isNotEmpty();
  }

  @Test
  void refWithoutNumberIsRejected() {
    assertThat(validator.validate(new GroupDTO(UUID.randomUUID(), "K"))).isNotEmpty();
  }

  @Test
  void refWithLeadingZeroIsRejected() {
    assertThat(validator.validate(new GroupDTO(UUID.randomUUID(), "K01"))).isNotEmpty();
  }

  @Test
  void refWithWrongPrefixIsRejected() {
    assertThat(validator.validate(new GroupDTO(UUID.randomUUID(), "Group1"))).isNotEmpty();
  }
}
