package com.example.demo.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.model.GradeHistoryDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtService;
import com.example.demo.security.SecurityConfig;
import com.example.demo.service.GradeHistoryService;
import com.example.demo.service.GradeSubmissionService;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GradeController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  CustomUserDetailsService.class
})
class GradeControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private GradeSubmissionService gradeSubmissionService;

  @MockBean private GradeHistoryService gradeHistoryService;

  @MockBean private UserRepository userRepository;

  @Test
  @WithMockUser(username = "t@hei.school", roles = "TEACHER")
  void submitValidGradeReturnsCreated() throws Exception {
    var studentId = UUID.randomUUID();
    var examId = UUID.randomUUID();
    when(gradeSubmissionService.recordGrade(any(), any(), any(Double.class), any()))
        .thenReturn(
            new GradeHistoryDTO(
                UUID.randomUUID(), studentId, examId, 15.0, Instant.now(), UUID.randomUUID()));

    mockMvc
        .perform(
            post("/exams/{id}/grades", examId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentId\":\"" + studentId + "\",\"value\":15.0}"))
        .andExpect(status().isCreated());
  }

  @Test
  @WithMockUser(username = "t@hei.school", roles = "TEACHER")
  void submitOutOfRangeGradeReturnsBadRequest() throws Exception {
    mockMvc
        .perform(
            post("/exams/{id}/grades", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"studentId\":\"" + UUID.randomUUID() + "\",\"value\":25.0}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(username = "s@hei.school", roles = "STUDENT")
  void getStudentGradesReturnsOnlyThatStudentsGrades() throws Exception {
    var studentId = UUID.randomUUID();
    when(gradeHistoryService.getGradesForStudent(studentId))
        .thenReturn(
            List.of(
                new GradeHistoryDTO(
                    UUID.randomUUID(),
                    studentId,
                    UUID.randomUUID(),
                    12.0,
                    Instant.now(),
                    UUID.randomUUID())));

    mockMvc.perform(get("/students/{id}/grades", studentId)).andExpect(status().isOk());
  }
}
