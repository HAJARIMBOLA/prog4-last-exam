package com.example.demo.endpoint.web.controller;

import com.example.demo.domain.Role;
import com.example.demo.model.LoginRequest;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebAuthController {

  private final AuthService authService;
  private final UserRepository userRepository;
  private final long jwtExpirationMs;

  public WebAuthController(
      AuthService authService,
      UserRepository userRepository,
      @Value("${jwt.expiration-ms}") long jwtExpirationMs) {
    this.authService = authService;
    this.userRepository = userRepository;
    this.jwtExpirationMs = jwtExpirationMs;
  }

  @GetMapping("/ui/login")
  public String loginForm() {
    return "login";
  }

  @PostMapping("/ui/login")
  public String login(
      @RequestParam String email,
      @RequestParam String password,
      Model model,
      HttpServletResponse response) {
    try {
      var loginResponse = authService.login(new LoginRequest(email, password));
      var cookie = new Cookie("auth_token", loginResponse.token());
      cookie.setHttpOnly(true);
      cookie.setPath("/");
      cookie.setMaxAge((int) (jwtExpirationMs / 1000));
      response.addCookie(cookie);

      var user = userRepository.findByEmail(email).orElseThrow();
      if (user.getRole() == Role.STUDENT) {
        return "redirect:/ui/students/" + user.getId() + "/transcripts";
      }
      return "redirect:/ui/promotions";
    } catch (BadCredentialsException e) {
      model.addAttribute("error", "Email ou mot de passe invalide");
      return "login";
    }
  }

  @GetMapping("/ui/logout")
  public String logout(HttpServletResponse response) {
    var cookie = new Cookie("auth_token", null);
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(0);
    response.addCookie(cookie);
    return "redirect:/ui/login";
  }
}
