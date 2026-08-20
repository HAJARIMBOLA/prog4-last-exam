package com.example.demo.endpoint.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.endpoint.event.EventProducer;
import com.example.demo.endpoint.event.model.ThreeYearTranscriptGenerationRequested;
import com.example.demo.model.TranscriptDTO;
import com.example.demo.model.TranscriptLineDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtService;
import com.example.demo.security.SecurityConfig;
import com.example.demo.service.ThreeYearTranscriptGenerationService;
import com.example.demo.service.TranscriptGenerationService;
import java.time.Instant;
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

@WebMvcTest(controllers = StudentTranscriptViewController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  CustomUserDetailsService.class
})
class StudentTranscriptViewControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private TranscriptGenerationService transcriptGenerationService;

  @MockBean private ThreeYearTranscriptGenerationService threeYearTranscriptGenerationService;

  @MockBean private EventProducer<ThreeYearTranscriptGenerationRequested> threeYearEventProducer;

  @MockBean private UserRepository userRepository;

  @Test
  @WithMockUser(username = "s@hei.school", roles = "STUDENT")
  void studentSeesTheirOwnProvisionalTranscriptWithARequestButton() throws Exception {
    var studentId = UUID.randomUUID();
    var academicYearId = UUID.randomUUID();
    when(userRepository.findByEmail("s@hei.school"))
        .thenReturn(Optional.of(studentWith(studentId, "s@hei.school")));
    when(transcriptGenerationService.generate(studentId, academicYearId))
        .thenReturn(
            new TranscriptDTO(
                studentId,
                academicYearId,
                Instant.now(),
                List.of(
                    new TranscriptLineDTO(
                        UUID.randomUUID(), "ALG101", "Algorithms", UUID.randomUUID(), 15.0)),
                true,
                null,
                4));

    mockMvc
        .perform(get("/ui/students/{id}/transcripts/{year}", studentId, academicYearId))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("PROVISIONAL")))
        .andExpect(content().string(containsString("ALG101")))
        .andExpect(content().string(containsString("request-transcript-button")))
        .andExpect(
            content()
                .string(
                    containsString("/students/" + studentId + "/transcripts/" + academicYearId)));
  }

  @Test
  @WithMockUser(username = "s@hei.school", roles = "STUDENT")
  void studentCannotViewAnotherStudentsTranscript() throws Exception {
    var ownId = UUID.randomUUID();
    var otherStudentId = UUID.randomUUID();
    when(userRepository.findByEmail("s@hei.school"))
        .thenReturn(Optional.of(studentWith(ownId, "s@hei.school")));

    mockMvc
        .perform(get("/ui/students/{id}/transcripts/{year}", otherStudentId, UUID.randomUUID()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "admin@hei.school", roles = "ADMIN")
  void adminCanViewAnyStudentsTranscript() throws Exception {
    var studentId = UUID.randomUUID();
    var academicYearId = UUID.randomUUID();
    when(transcriptGenerationService.generate(studentId, academicYearId))
        .thenReturn(
            new TranscriptDTO(
                studentId, academicYearId, Instant.now(), List.of(), false, 15.0, 60));

    mockMvc
        .perform(get("/ui/students/{id}/transcripts/{year}", studentId, academicYearId))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("COMPLETE")));
  }

  @Test
  @WithMockUser(username = "s@hei.school", roles = "STUDENT")
  void studentSeesTheirOwnYearsListWithADownloadButton() throws Exception {
    var studentId = UUID.randomUUID();
    var year1 = UUID.randomUUID();
    var year2 = UUID.randomUUID();
    when(userRepository.findByEmail("s@hei.school"))
        .thenReturn(Optional.of(studentWith(studentId, "s@hei.school")));
    when(userRepository.findById(studentId))
        .thenReturn(Optional.of(studentWith(studentId, "s@hei.school")));
    when(threeYearTranscriptGenerationService.resolveOrderedAcademicYearIds(studentId))
        .thenReturn(List.of(year1, year2));

    mockMvc
        .perform(get("/ui/students/{id}/transcripts", studentId))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("view-year-transcript-link")))
        .andExpect(content().string(containsString("request-full-transcript-button")))
        .andExpect(content().string(containsString(year1.toString())))
        .andExpect(content().string(containsString(year2.toString())))
        .andExpect(content().string(containsString("Jane Doe")))
        .andExpect(content().string(containsString("STD001")));
  }

  @Test
  @WithMockUser(username = "s@hei.school", roles = "STUDENT")
  void studentCannotListAnotherStudentsYears() throws Exception {
    var ownId = UUID.randomUUID();
    var otherStudentId = UUID.randomUUID();
    when(userRepository.findByEmail("s@hei.school"))
        .thenReturn(Optional.of(studentWith(ownId, "s@hei.school")));

    mockMvc
        .perform(get("/ui/students/{id}/transcripts", otherStudentId))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(username = "s@hei.school", roles = "STUDENT")
  void requestingTheirOwnFullTranscriptRedirectsWithAConfirmation() throws Exception {
    var studentId = UUID.randomUUID();
    when(userRepository.findByEmail("s@hei.school"))
        .thenReturn(Optional.of(studentWith(studentId, "s@hei.school")));

    mockMvc
        .perform(post("/ui/students/{id}/transcripts/full", studentId).with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/ui/students/" + studentId + "/transcripts?sent=true"));

    verify(threeYearEventProducer).accept(any());
  }

  @Test
  @WithMockUser(username = "s@hei.school", roles = "STUDENT")
  void studentCannotRequestAnotherStudentsFullTranscript() throws Exception {
    var ownId = UUID.randomUUID();
    var otherStudentId = UUID.randomUUID();
    when(userRepository.findByEmail("s@hei.school"))
        .thenReturn(Optional.of(studentWith(ownId, "s@hei.school")));

    mockMvc
        .perform(post("/ui/students/{id}/transcripts/full", otherStudentId).with(csrf()))
        .andExpect(status().isForbidden());
  }

  private User studentWith(UUID id, String email) {
    return new User(id, email, "hash", Role.STUDENT, "STD001", "Jane", "Doe");
  }
}
