package com.example.demo.service;

import com.example.demo.model.LoginRequest;
import com.example.demo.model.LoginResponse;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;

  public LoginResponse login(LoginRequest request) {
    var user =
        userRepository
            .findByEmail(request.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new BadCredentialsException("Invalid credentials");
    }
    return new LoginResponse(jwtService.generateToken(user));
  }
}
