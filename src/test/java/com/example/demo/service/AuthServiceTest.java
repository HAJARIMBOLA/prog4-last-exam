package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.model.LoginRequest;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private JwtService jwtService;

  @Test
  void loginWithValidCredentialsReturnsToken() {
    var user =
        new User(
            UUID.randomUUID(),
            "student@hei.school",
            "hashed",
            Role.STUDENT,
            "STD001",
            "Jane",
            "Doe");
    when(userRepository.findByEmail("student@hei.school")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
    when(jwtService.generateToken(user)).thenReturn("jwt-token");

    var authService = new AuthService(userRepository, passwordEncoder, jwtService);
    var response = authService.login(new LoginRequest("student@hei.school", "secret"));

    assertThat(response.token()).isEqualTo("jwt-token");
  }

  @Test
  void loginWithUnknownEmailThrowsBadCredentials() {
    when(userRepository.findByEmail("unknown@hei.school")).thenReturn(Optional.empty());
    var authService = new AuthService(userRepository, passwordEncoder, jwtService);

    assertThatThrownBy(() -> authService.login(new LoginRequest("unknown@hei.school", "secret")))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void loginWithWrongPasswordThrowsBadCredentials() {
    var user =
        new User(
            UUID.randomUUID(),
            "student@hei.school",
            "hashed",
            Role.STUDENT,
            "STD001",
            "Jane",
            "Doe");
    when(userRepository.findByEmail("student@hei.school")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);
    var authService = new AuthService(userRepository, passwordEncoder, jwtService);

    assertThatThrownBy(() -> authService.login(new LoginRequest("student@hei.school", "wrong")))
        .isInstanceOf(BadCredentialsException.class);
  }
}
