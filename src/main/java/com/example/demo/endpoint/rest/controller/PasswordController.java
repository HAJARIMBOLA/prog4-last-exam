package com.example.demo.endpoint.rest.controller;

import com.example.demo.model.ChangePasswordRequest;
import com.example.demo.model.ResetPasswordResponse;
import com.example.demo.service.PasswordService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class PasswordController {

  private final PasswordService passwordService;

  @PatchMapping("/users/me/password")
  public ResponseEntity<Void> changeOwnPassword(
      Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {
    try {
      passwordService.changeOwnPassword(authentication.getName(), request);
      return ResponseEntity.noContent().build();
    } catch (BadCredentialsException e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }

  @PostMapping("/admin/users/{id}/reset-password")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ResetPasswordResponse> resetPassword(@PathVariable UUID id) {
    return ResponseEntity.ok(passwordService.resetPassword(id));
  }
}
