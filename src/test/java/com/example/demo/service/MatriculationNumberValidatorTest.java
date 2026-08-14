package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatriculationNumberValidatorTest {

  @Mock private UserRepository userRepository;

  @Test
  void acceptsAUniqueWellFormattedNumber() {
    when(userRepository.existsByMatriculationNumber("STD001")).thenReturn(false);
    var validator = new MatriculationNumberValidator(userRepository);

    assertThatCode(() -> validator.validate("STD001")).doesNotThrowAnyException();
  }

  @Test
  void rejectsADuplicateNumber() {
    when(userRepository.existsByMatriculationNumber("STD001")).thenReturn(true);
    var validator = new MatriculationNumberValidator(userRepository);

    assertThatThrownBy(() -> validator.validate("STD001"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsAMalformedNumber() {
    var validator = new MatriculationNumberValidator(userRepository);

    assertThatThrownBy(() -> validator.validate("STUDENT001"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
