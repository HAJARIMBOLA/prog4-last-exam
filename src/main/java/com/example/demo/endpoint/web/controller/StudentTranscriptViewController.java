package com.example.demo.endpoint.web.controller;

import com.example.demo.endpoint.event.EventProducer;
import com.example.demo.endpoint.event.model.ThreeYearTranscriptGenerationRequested;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ThreeYearTranscriptGenerationService;
import com.example.demo.service.TranscriptGenerationService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@AllArgsConstructor
public class StudentTranscriptViewController {

  private static final String STUDENT_AUTHORITY = "ROLE_STUDENT";

  private final TranscriptGenerationService transcriptGenerationService;
  private final ThreeYearTranscriptGenerationService threeYearTranscriptGenerationService;
  private final EventProducer<ThreeYearTranscriptGenerationRequested> threeYearEventProducer;
  private final UserRepository userRepository;

  @GetMapping("/ui/students/{id}/transcripts/{academicYearId}")
  public String viewTranscript(
      @PathVariable UUID id,
      @PathVariable UUID academicYearId,
      Authentication authentication,
      Model model) {
    requireSelfOrNonStudent(id, authentication);

    model.addAttribute("transcript", transcriptGenerationService.generate(id, academicYearId));
    model.addAttribute("studentId", id);
    model.addAttribute("academicYearId", academicYearId);
    return "transcript";
  }

  @GetMapping("/ui/students/{id}/transcripts")
  public String listTranscripts(@PathVariable UUID id, Authentication authentication, Model model) {
    requireSelfOrNonStudent(id, authentication);

    model.addAttribute("studentId", id);
    model.addAttribute(
        "academicYearIds", threeYearTranscriptGenerationService.resolveOrderedAcademicYearIds(id));
    return "student-transcripts";
  }

  @PostMapping("/ui/students/{id}/transcripts/full")
  public String requestFullTranscript(@PathVariable UUID id, Authentication authentication) {
    requireSelfOrNonStudent(id, authentication);

    var event = ThreeYearTranscriptGenerationRequested.builder().studentId(id).build();
    threeYearEventProducer.accept(List.of(event));

    return "redirect:/ui/students/{id}/transcripts?sent=true";
  }

  private void requireSelfOrNonStudent(UUID id, Authentication authentication) {
    var isStudent =
        authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(STUDENT_AUTHORITY));
    if (isStudent) {
      var requester =
          userRepository
              .findByEmail(authentication.getName())
              .orElseThrow(() -> new AccessDeniedException("Unknown authenticated user"));
      if (!requester.getId().equals(id)) {
        throw new AccessDeniedException("Students can only view their own transcript");
      }
    }
  }
}
