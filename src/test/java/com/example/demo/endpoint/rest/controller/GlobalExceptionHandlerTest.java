package com.example.demo.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.exception.InvalidCoefficientSumException;
import com.example.demo.exception.InvalidCreditStructureException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.StudentNotEnrolledException;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtService;
import com.example.demo.security.SecurityConfig;
import com.example.demo.service.AcademicYearClosingService;
import com.example.demo.service.CourseService;
import com.example.demo.service.ExamCreationService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
    controllers = {
      ExamController.class,
      CourseController.class,
      AcademicYearClosingController.class
    })
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  CustomUserDetailsService.class
})
class GlobalExceptionHandlerTest {

  private static final String EXAM_BODY = "{\"dateExam\":\"2026-06-01\",\"coefficient\":0.5}";

  @Autowired private MockMvc mockMvc;

  @MockBean private ExamCreationService examCreationService;
  @MockBean private CourseService courseService;
  @MockBean private AcademicYearClosingService academicYearClosingService;
  @MockBean private UserRepository userRepository;

  @Test
  @WithMockUser(roles = "ADMIN")
  void notFoundExceptionMapsTo404() throws Exception {
    when(examCreationService.createExam(any(), any()))
        .thenThrow(new NotFoundException("Course not found: nope"));

    mockMvc
        .perform(
            post("/courses/{courseId}/exams", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(EXAM_BODY)
                .with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("Course not found: nope"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void studentNotEnrolledExceptionMapsTo404() throws Exception {
    when(academicYearClosingService.closeAcademicYear(any()))
        .thenThrow(new StudentNotEnrolledException("No enrollment found for student x"));

    mockMvc
        .perform(post("/academic-years/{id}/close", UUID.randomUUID()).with(csrf()))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.detail").value("No enrollment found for student x"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void invalidCoefficientSumExceptionMapsTo409() throws Exception {
    when(examCreationService.createExam(any(), any()))
        .thenThrow(new InvalidCoefficientSumException("would exceed 1.0"));

    mockMvc
        .perform(
            post("/courses/{courseId}/exams", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(EXAM_BODY)
                .with(csrf()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.detail").value("would exceed 1.0"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void invalidCreditStructureExceptionMapsTo409() throws Exception {
    when(courseService.createCourse(any()))
        .thenThrow(new InvalidCreditStructureException("exceeds 30 credits"));

    mockMvc
        .perform(
            post("/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCourseCreationBody())
                .with(csrf()))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value("exceeds 30 credits"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void plainIllegalArgumentExceptionMapsTo400() throws Exception {
    when(courseService.createCourse(any())).thenThrow(new IllegalArgumentException("bad input"));

    mockMvc
        .perform(
            post("/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validCourseCreationBody())
                .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("bad input"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void malformedRequestBodyMapsTo400WithAValidationDetail() throws Exception {
    mockMvc
        .perform(
            post("/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"ref\":\"\",\"title\":\"Algorithms\",\"credits\":4,"
                        + "\"academicYearId\":\""
                        + UUID.randomUUID()
                        + "\",\"semesterId\":\""
                        + UUID.randomUUID()
                        + "\",\"tracks\":[\"EL\"],\"teacherIds\":[\""
                        + UUID.randomUUID()
                        + "\"]}")
                .with(csrf()))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Validation failed"))
        .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("ref")));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void malformedPathVariableMapsTo400() throws Exception {
    mockMvc.perform(get("/courses/{id}", "not-a-uuid")).andExpect(status().isBadRequest());
  }

  private String validCourseCreationBody() {
    return "{\"ref\":\"ALG101\",\"title\":\"Algorithms\",\"credits\":4,"
        + "\"academicYearId\":\""
        + UUID.randomUUID()
        + "\",\"semesterId\":\""
        + UUID.randomUUID()
        + "\",\"tracks\":[\"EL\"],\"teacherIds\":[\""
        + UUID.randomUUID()
        + "\"]}";
  }
}
