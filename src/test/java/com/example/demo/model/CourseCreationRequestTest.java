package com.example.demo.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Track;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CourseCreationRequestTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void validRequestWithOneTeacherHasNoViolations() {
    var request =
        new CourseCreationRequest(
            "ALG101",
            "Algorithms",
            4,
            UUID.randomUUID(),
            List.of(Track.COMMON_CORE),
            List.of(UUID.randomUUID()));
    assertThat(validator.validate(request)).isEmpty();
  }

  @Test
  void validRequestWithSeveralTeachersHasNoViolations() {
    var request =
        new CourseCreationRequest(
            "ALG101",
            "Algorithms",
            4,
            UUID.randomUUID(),
            List.of(Track.EL, Track.TN),
            List.of(UUID.randomUUID(), UUID.randomUUID()));
    assertThat(validator.validate(request)).isEmpty();
  }

  @Test
  void emptyTeacherListIsRejected() {
    var request =
        new CourseCreationRequest(
            "ALG101", "Algorithms", 4, UUID.randomUUID(), List.of(Track.EL), List.of());
    assertThat(validator.validate(request)).isNotEmpty();
  }

  @Test
  void emptyTrackListIsRejected() {
    var request =
        new CourseCreationRequest(
            "ALG101", "Algorithms", 4, UUID.randomUUID(), List.of(), List.of(UUID.randomUUID()));
    assertThat(validator.validate(request)).isNotEmpty();
  }
}
