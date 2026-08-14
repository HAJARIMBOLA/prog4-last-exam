package com.example.demo.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

class JwtServiceTest {

  private final JwtService jwtService =
      new JwtService("test-secret-key-not-for-production-use-0123456789abcdef", 3_600_000);

  @Test
  void generatedTokenContainsUserEmailAsSubject() {
    var user =
        new User(UUID.randomUUID(), "a@b.com", "hash", Role.STUDENT, "STD001", "Jane", "Doe");
    var token = jwtService.generateToken(user);
    assertThat(jwtService.extractEmail(token)).isEqualTo("a@b.com");
  }

  @Test
  void tokenIsValidForMatchingUser() {
    var user =
        new User(UUID.randomUUID(), "a@b.com", "hash", Role.STUDENT, "STD001", "Jane", "Doe");
    var token = jwtService.generateToken(user);
    UserDetails userDetails =
        org.springframework.security.core.userdetails.User.withUsername("a@b.com")
            .password("hash")
            .authorities("ROLE_STUDENT")
            .build();
    assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
  }
}
