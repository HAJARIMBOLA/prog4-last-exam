package com.example.demo.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.model.CourseDTO;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtService;
import com.example.demo.service.CourseService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CourseController.class)
@AutoConfigureMockMvc(addFilters = false)
class CourseControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private CourseService courseService;

  @MockBean private JwtService jwtService;

  @MockBean private CustomUserDetailsService userDetailsService;

  @Test
  void listCoursesReturnsCourseDTOShape() throws Exception {
    var courseId = UUID.randomUUID();
    when(courseService.listCourses())
        .thenReturn(List.of(new CourseDTO(courseId, "ALG101", "Algorithms", 4)));

    mockMvc
        .perform(get("/courses"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(courseId.toString()))
        .andExpect(jsonPath("$[0].ref").value("ALG101"))
        .andExpect(jsonPath("$[0].title").value("Algorithms"))
        .andExpect(jsonPath("$[0].credits").value(4));
  }

  @Test
  void getCourseByIdReturnsCourseDTOShape() throws Exception {
    var courseId = UUID.randomUUID();
    when(courseService.findCourse(courseId))
        .thenReturn(Optional.of(new CourseDTO(courseId, "ALG101", "Algorithms", 4)));

    mockMvc
        .perform(get("/courses/{id}", courseId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.ref").value("ALG101"))
        .andExpect(jsonPath("$.title").value("Algorithms"))
        .andExpect(jsonPath("$.credits").value(4));
  }

  @Test
  void getUnknownCourseReturnsNotFound() throws Exception {
    when(courseService.findCourse(any())).thenReturn(Optional.empty());

    mockMvc.perform(get("/courses/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
  }
}
