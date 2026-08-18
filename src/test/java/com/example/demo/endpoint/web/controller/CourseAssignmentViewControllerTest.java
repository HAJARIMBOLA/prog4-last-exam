package com.example.demo.endpoint.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.model.CourseAssignmentDTO;
import com.example.demo.model.CourseDTO;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtService;
import com.example.demo.security.SecurityConfig;
import com.example.demo.service.CourseAssignmentService;
import com.example.demo.service.CourseService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = CourseAssignmentViewController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  CustomUserDetailsService.class
})
class CourseAssignmentViewControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private CourseAssignmentService courseAssignmentService;
  @MockBean private CourseService courseService;
  @MockBean private UserRepository userRepository;
  @MockBean private AcademicYearRepository academicYearRepository;

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminSeesTheAssignmentListAndTheCreateForm() throws Exception {
    var courseId = UUID.randomUUID();
    var teacherId = UUID.randomUUID();
    when(courseService.findCourse(courseId))
        .thenReturn(Optional.of(new CourseDTO(courseId, "ALG101", "Algorithms", 4)));
    when(courseAssignmentService.listForCourse(courseId))
        .thenReturn(
            List.of(
                new CourseAssignmentDTO(
                    UUID.randomUUID(), courseId, teacherId, UUID.randomUUID())));
    when(userRepository.findByRole(Role.TEACHER))
        .thenReturn(
            List.of(
                new User(teacherId, "t@hei.school", "hash", Role.TEACHER, null, "Jane", "Doe")));
    when(academicYearRepository.findAll())
        .thenReturn(List.of(new AcademicYear(UUID.randomUUID(), LocalDate.now(), LocalDate.now())));

    mockMvc
        .perform(get("/ui/courses/{courseId}/assignments", courseId))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("ALG101")))
        .andExpect(content().string(containsString("create-assignment-form")))
        .andExpect(content().string(containsString("update-assignment-form")));
  }

  @Test
  @WithMockUser(roles = "TEACHER")
  void teacherCannotAccessTheAssignmentView() throws Exception {
    mockMvc
        .perform(get("/ui/courses/{courseId}/assignments", UUID.randomUUID()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void creatingAnAssignmentRedirectsBackToTheList() throws Exception {
    var courseId = UUID.randomUUID();
    var teacherId = UUID.randomUUID();
    var academicYearId = UUID.randomUUID();

    mockMvc
        .perform(
            post("/ui/courses/{courseId}/assignments", courseId)
                .param("teacherId", teacherId.toString())
                .param("academicYearId", academicYearId.toString())
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/ui/courses/" + courseId + "/assignments"));

    verify(courseAssignmentService).createAssignment(courseId, teacherId, academicYearId);
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void updatingAnAssignmentRedirectsBackToTheList() throws Exception {
    var courseId = UUID.randomUUID();
    var assignmentId = UUID.randomUUID();
    var newTeacherId = UUID.randomUUID();

    mockMvc
        .perform(
            post("/ui/courses/{courseId}/assignments/{assignmentId}", courseId, assignmentId)
                .param("teacherId", newTeacherId.toString())
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/ui/courses/" + courseId + "/assignments"));

    verify(courseAssignmentService).updateTeacher(assignmentId, newTeacherId);
  }
}
