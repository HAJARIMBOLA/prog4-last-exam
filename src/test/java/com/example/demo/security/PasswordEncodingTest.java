package com.example.demo.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordEncodingTest {

  private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

  @Test
  void encodedPasswordIsNeverStoredInPlainText() {
    var hash = encoder.encode("s3cret!");
    assertThat(hash).isNotEqualTo("s3cret!");
  }

  @Test
  void matchesReturnsTrueForCorrectPassword() {
    var hash = encoder.encode("s3cret!");
    assertThat(encoder.matches("s3cret!", hash)).isTrue();
  }

  @Test
  void matchesReturnsFalseForWrongPassword() {
    var hash = encoder.encode("s3cret!");
    assertThat(encoder.matches("wrong", hash)).isFalse();
  }
}
