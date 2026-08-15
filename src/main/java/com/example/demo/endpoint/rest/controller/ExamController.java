package com.example.demo.endpoint.rest.controller;

import com.example.demo.model.ExamCreationRequest;
import com.example.demo.model.ExamDTO;
import com.example.demo.service.ExamCreationService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ExamController {

  private final ExamCreationService examCreationService;

  @PostMapping("/courses/{courseId}/exams")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ExamDTO> createExam(
      @PathVariable UUID courseId, @Valid @RequestBody ExamCreationRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(examCreationService.createExam(courseId, request));
  }
}
