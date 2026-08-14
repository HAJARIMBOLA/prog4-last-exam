package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.model.ChangePasswordRequest;
import com.example.demo.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @Test
  void changeOwnPasswordWithCorrectCurrentPasswordUpdatesHash() {
    var user =
        new User(UUID.randomUUID(), "a@b.com", "old-hash", Role.STUDENT, "STD001", "Jane", "Doe");
    when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("old", "old-hash")).thenReturn(true);
    when(passwordEncoder.encode("newpassword")).thenReturn("new-hash");

    var passwordService = new PasswordService(userRepository, passwordEncoder);
    passwordService.changeOwnPassword("a@b.com", new ChangePasswordRequest("old", "newpassword"));

    assertThat(user.getPasswordHash()).isEqualTo("new-hash");
    verify(userRepository).save(user);
  }

  @Test
  void changeOwnPasswordWithWrongCurrentPasswordThrows() {
    var user =
        new User(UUID.randomUUID(), "a@b.com", "old-hash", Role.STUDENT, "STD001", "Jane", "Doe");
    when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong", "old-hash")).thenReturn(false);

    var passwordService = new PasswordService(userRepository, passwordEncoder);

    assertThatThrownBy(
            () ->
                passwordService.changeOwnPassword(
                    "a@b.com", new ChangePasswordRequest("wrong", "newpassword")))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void resetPasswordWorksRegardlessOfOldPassword() {
    var userId = UUID.randomUUID();
    var user = new User(userId, "a@b.com", "old-hash", Role.STUDENT, "STD001", "Jane", "Doe");
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode(any())).thenReturn("temp-hash");

    var passwordService = new PasswordService(userRepository, passwordEncoder);
    var response = passwordService.resetPassword(userId);

    assertThat(response.temporaryPassword()).isNotBlank();
    assertThat(user.getPasswordHash()).isEqualTo("temp-hash");
  }
}
