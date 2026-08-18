package com.example.demo.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.endpoint.event.EventProducer;
import com.example.demo.endpoint.event.model.TranscriptGenerationRequested;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtService;
import com.example.demo.security.SecurityConfig;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = TranscriptController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  CustomUserDetailsService.class
})
class TranscriptControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private EventProducer<TranscriptGenerationRequested> eventProducer;

  @MockBean private UserRepository userRepository;

  @Test
  @WithMockUser(username = "s@hei.school", roles = "STUDENT")
  void studentCanRequestTheirOwnTranscript() throws Exception {
    var studentId = UUID.randomUUID();
    when(userRepository.findByEmail("s@hei.school"))
        .thenReturn(Optional.of(studentWith(studentId, "s@hei.school")));

    mockMvc
        .perform(
            post("/students/{id}/transcripts/{year}", studentId, UUID.randomUUID()).with(csrf()))
        .andExpect(status().isAccepted());

    verify(eventProducer).accept(any());
  }

  @Test
  @WithMockUser(username = "s@hei.school", roles = "STUDENT")
  void studentCannotRequestAnotherStudentsTranscript() throws Exception {
    var ownId = UUID.randomUUID();
    var otherStudentId = UUID.randomUUID();
    when(userRepository.findByEmail("s@hei.school"))
        .thenReturn(Optional.of(studentWith(ownId, "s@hei.school")));

    mockMvc
        .perform(
            post("/students/{id}/transcripts/{year}", otherStudentId, UUID.randomUUID())
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "admin@hei.school", roles = "ADMIN")
  void adminCanRequestAnyStudentsTranscript() throws Exception {
    mockMvc
        .perform(
            post("/students/{id}/transcripts/{year}", UUID.randomUUID(), UUID.randomUUID())
                .with(csrf()))
        .andExpect(status().isAccepted());

    verify(eventProducer).accept(any());
  }

  private User studentWith(UUID id, String email) {
    return new User(id, email, "hash", Role.STUDENT, "STD001", "Jane", "Doe");
  }
}
