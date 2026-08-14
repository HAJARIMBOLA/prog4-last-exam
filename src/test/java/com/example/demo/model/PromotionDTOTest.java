package com.example.demo.model;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PromotionDTOTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void validPromotionHasNoViolations() {
    var promotion = new PromotionDTO(UUID.randomUUID(), "Promotion 2026", 2026);
    assertThat(validator.validate(promotion)).isEmpty();
  }

  @Test
  void blankLabelIsRejected() {
    var promotion = new PromotionDTO(UUID.randomUUID(), " ", 2026);
    assertThat(validator.validate(promotion)).isNotEmpty();
  }

  @Test
  void nullExpectedGraduationYearIsRejected() {
    var promotion = new PromotionDTO(UUID.randomUUID(), "Promotion 2026", null);
    assertThat(validator.validate(promotion)).isNotEmpty();
  }

  @Test
  void nonPositiveExpectedGraduationYearIsRejected() {
    var promotion = new PromotionDTO(UUID.randomUUID(), "Promotion 2026", -1);
    assertThat(validator.validate(promotion)).isNotEmpty();
  }
}
