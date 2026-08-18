package com.example.demo.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.model.ExamDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtService;
import com.example.demo.security.SecurityConfig;
import com.example.demo.service.ExamCreationService;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ExamController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  CustomUserDetailsService.class
})
class ExamControllerTest {

  private static final String EXAM_BODY = "{\"dateExam\":\"2026-06-01\",\"coefficient\":0.5}";

  @Autowired private MockMvc mockMvc;

  @MockBean private ExamCreationService examCreationService;

  @MockBean private UserRepository userRepository;

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCanCreateAnExam() throws Exception {
    var courseId = UUID.randomUUID();
    when(examCreationService.createExam(eq(courseId), any()))
        .thenReturn(new ExamDTO(UUID.randomUUID(), LocalDate.of(2026, 6, 1), 0.5, courseId));

    mockMvc
        .perform(
            post("/courses/{courseId}/exams", courseId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(EXAM_BODY)
                .with(csrf()))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser(roles = "TEACHER")
  void teacherCannotCreateAnExam() throws Exception {
    mockMvc
        .perform(
            post("/courses/{courseId}/exams", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(EXAM_BODY)
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "STUDENT")
  void studentCannotCreateAnExam() throws Exception {
    mockMvc
        .perform(
            post("/courses/{courseId}/exams", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(EXAM_BODY)
                .with(csrf()))
        .andExpect(status().isForbidden());
  }
}
