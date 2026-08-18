package com.example.demo.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.domain.Role;
import com.example.demo.model.UserCreationResponse;
import com.example.demo.model.UserDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtService;
import com.example.demo.security.SecurityConfig;
import com.example.demo.service.UserAdminService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AdminUserController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  CustomUserDetailsService.class
})
class AdminUserControllerTest {

  private static final String STUDENT_BODY =
      "{\"email\":\"s@hei.school\",\"firstName\":\"Jane\",\"lastName\":\"Doe\",\"matriculationNumber\":\"STD001\"}";
  private static final String TEACHER_BODY =
      "{\"email\":\"t@hei.school\",\"firstName\":\"John\",\"lastName\":\"Smith\"}";

  @Autowired private MockMvc mockMvc;

  @MockBean private UserAdminService userAdminService;

  @MockBean private UserRepository userRepository;

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCanCreateStudent() throws Exception {
    when(userAdminService.createStudent(any()))
        .thenReturn(
            new UserCreationResponse(
                new UserDTO(
                    UUID.randomUUID(), "s@hei.school", Role.STUDENT, "STD001", "Jane", "Doe"),
                "Ab12Cd34Ef56"));

    mockMvc
        .perform(
            post("/admin/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(STUDENT_BODY)
                .with(csrf()))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCanUpdateStudent() throws Exception {
    when(userAdminService.updateStudent(any(), any()))
        .thenReturn(
            new UserDTO(UUID.randomUUID(), "s@hei.school", Role.STUDENT, "STD001", "Jane", "Doe"));

    mockMvc
        .perform(
            put("/admin/students/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(STUDENT_BODY)
                .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCanCreateTeacher() throws Exception {
    when(userAdminService.createTeacher(any()))
        .thenReturn(
            new UserCreationResponse(
                new UserDTO(UUID.randomUUID(), "t@hei.school", Role.TEACHER, null, "John", "Smith"),
                "Ab12Cd34Ef56"));

    mockMvc
        .perform(
            post("/admin/teachers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TEACHER_BODY)
                .with(csrf()))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCanUpdateTeacher() throws Exception {
    when(userAdminService.updateTeacher(any(), any()))
        .thenReturn(
            new UserDTO(UUID.randomUUID(), "t@hei.school", Role.TEACHER, null, "John", "Smith"));

    mockMvc
        .perform(
            put("/admin/teachers/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(TEACHER_BODY)
                .with(csrf()))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "STUDENT")
  void studentCannotCreateStudent() throws Exception {
    mockMvc
        .perform(
            post("/admin/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(STUDENT_BODY)
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "TEACHER")
  void teacherCannotUpdateStudent() throws Exception {
    mockMvc
        .perform(
            put("/admin/students/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(STUDENT_BODY)
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "STUDENT")
  void studentCannotCreateTeacher() throws Exception {
    mockMvc
        .perform(
            post("/admin/teachers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TEACHER_BODY)
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "TEACHER")
  void teacherCannotUpdateTeacher() throws Exception {
    mockMvc
        .perform(
            put("/admin/teachers/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(TEACHER_BODY)
                .with(csrf()))
        .andExpect(status().isForbidden());
  }
}
