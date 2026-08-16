package com.example.demo.endpoint.web.controller;

import com.example.demo.repository.UserRepository;
import com.example.demo.service.TranscriptGenerationService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@AllArgsConstructor
public class StudentTranscriptViewController {

  private static final String STUDENT_AUTHORITY = "ROLE_STUDENT";

  private final TranscriptGenerationService transcriptGenerationService;
  private final UserRepository userRepository;

  @GetMapping("/ui/students/{id}/transcripts/{academicYearId}")
  public String viewTranscript(
      @PathVariable UUID id,
      @PathVariable UUID academicYearId,
      Authentication authentication,
      Model model) {
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

    model.addAttribute("transcript", transcriptGenerationService.generate(id, academicYearId));
    model.addAttribute("studentId", id);
    model.addAttribute("academicYearId", academicYearId);
    return "transcript";
  }
}
