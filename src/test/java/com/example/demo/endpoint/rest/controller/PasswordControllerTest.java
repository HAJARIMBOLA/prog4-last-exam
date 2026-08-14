package com.example.demo.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.model.ResetPasswordResponse;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtService;
import com.example.demo.security.SecurityConfig;
import com.example.demo.service.PasswordService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PasswordController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  CustomUserDetailsService.class
})
class PasswordControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PasswordService passwordService;

  @MockBean private UserRepository userRepository;

  @Test
  @WithMockUser(username = "alice@hei.school", roles = "STUDENT")
  void changeOwnPasswordWithCorrectCurrentPasswordReturnsNoContent() throws Exception {
    mockMvc
        .perform(
            patch("/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"old\",\"newPassword\":\"newpassword123\"}"))
        .andExpect(status().isNoContent());
  }

  @Test
  @WithMockUser(username = "alice@hei.school", roles = "STUDENT")
  void changeOwnPasswordWithWrongCurrentPasswordReturnsUnauthorized() throws Exception {
    doThrow(new BadCredentialsException("Invalid credentials"))
        .when(passwordService)
        .changeOwnPassword(eq("alice@hei.school"), any());

    mockMvc
        .perform(
            patch("/users/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"currentPassword\":\"wrong\",\"newPassword\":\"newpassword123\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void resetPasswordAsAdminReturnsOk() throws Exception {
    when(passwordService.resetPassword(any()))
        .thenReturn(new ResetPasswordResponse("Ab12Cd34Ef56"));

    mockMvc
        .perform(post("/admin/users/{id}/reset-password", UUID.randomUUID()))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "STUDENT")
  void resetPasswordAsNonAdminReturnsForbidden() throws Exception {
    mockMvc
        .perform(post("/admin/users/{id}/reset-password", UUID.randomUUID()))
        .andExpect(status().isForbidden());
  }
}
