package com.example.demo.endpoint.rest.controller;

import com.example.demo.endpoint.event.EventProducer;
import com.example.demo.endpoint.event.model.ThreeYearTranscriptGenerationRequested;
import com.example.demo.endpoint.event.model.TranscriptGenerationRequested;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class TranscriptController {

  private static final String STUDENT_AUTHORITY = "ROLE_STUDENT";

  private final EventProducer<TranscriptGenerationRequested> eventProducer;
  private final EventProducer<ThreeYearTranscriptGenerationRequested> threeYearEventProducer;
  private final UserRepository userRepository;

  @PostMapping("/students/{id}/transcripts/{year}")
  public ResponseEntity<Void> requestTranscript(
      @PathVariable UUID id, @PathVariable UUID year, Authentication authentication) {
    requireSelfOrNonStudent(id, authentication);

    var event = TranscriptGenerationRequested.builder().studentId(id).academicYearId(year).build();
    eventProducer.accept(List.of(event));

    return ResponseEntity.accepted().build();
  }

  @PostMapping("/students/{id}/transcripts/full")
  public ResponseEntity<Void> requestFullTranscript(
      @PathVariable UUID id, Authentication authentication) {
    requireSelfOrNonStudent(id, authentication);

    var event = ThreeYearTranscriptGenerationRequested.builder().studentId(id).build();
    threeYearEventProducer.accept(List.of(event));

    return ResponseEntity.accepted().build();
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
        throw new AccessDeniedException("Students can only request their own transcript");
      }
    }
  }
}
