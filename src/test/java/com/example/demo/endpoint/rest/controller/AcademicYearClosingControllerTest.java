package com.example.demo.endpoint.rest.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtService;
import com.example.demo.security.SecurityConfig;
import com.example.demo.service.AcademicYearClosingService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AcademicYearClosingController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  CustomUserDetailsService.class
})
class AcademicYearClosingControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private AcademicYearClosingService academicYearClosingService;

  @MockBean private UserRepository userRepository;

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCanCloseAnAcademicYear() throws Exception {
    var academicYearId = UUID.randomUUID();
    when(academicYearClosingService.closeAcademicYear(academicYearId))
        .thenReturn(List.of(UUID.randomUUID(), UUID.randomUUID()));

    mockMvc
        .perform(post("/academic-years/{id}/close", academicYearId).with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.studentsProcessed").value(2));
  }

  @Test
  @WithMockUser(roles = "TEACHER")
  void teacherCannotCloseAnAcademicYear() throws Exception {
    mockMvc
        .perform(post("/academic-years/{id}/close", UUID.randomUUID()).with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "STUDENT")
  void studentCannotCloseAnAcademicYear() throws Exception {
    mockMvc
        .perform(post("/academic-years/{id}/close", UUID.randomUUID()).with(csrf()))
        .andExpect(status().isForbidden());
  }
}
