package com.example.demo.service;

import com.example.demo.model.ChangePasswordRequest;
import com.example.demo.model.ResetPasswordResponse;
import com.example.demo.repository.UserRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PasswordService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public void changeOwnPassword(String email, ChangePasswordRequest request) {
    var user =
        userRepository
            .findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
      throw new BadCredentialsException("Invalid credentials");
    }
    user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);
  }

  public ResetPasswordResponse resetPassword(UUID userId) {
    var user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    var temporaryPassword = TemporaryPasswordGenerator.generate();
    user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
    userRepository.save(user);
    return new ResetPasswordResponse(temporaryPassword);
  }
}
