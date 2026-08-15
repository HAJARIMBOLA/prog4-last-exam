package com.example.demo.endpoint.rest.controller;

import com.example.demo.model.GradeHistoryDTO;
import com.example.demo.model.GradeSubmissionRequest;
import com.example.demo.service.GradeHistoryService;
import com.example.demo.service.GradeSubmissionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class GradeController {

  private final GradeSubmissionService gradeSubmissionService;
  private final GradeHistoryService gradeHistoryService;

  @PostMapping("/exams/{id}/grades")
  public ResponseEntity<GradeHistoryDTO> submitGrade(
      @PathVariable UUID id,
      @Valid @RequestBody GradeSubmissionRequest request,
      Authentication authentication) {
    var recorded =
        gradeSubmissionService.recordGrade(
            id, request.studentId(), request.value(), authentication.getName());
    return ResponseEntity.status(HttpStatus.CREATED).body(recorded);
  }

  @GetMapping("/students/{id}/grades")
  public ResponseEntity<List<GradeHistoryDTO>> getStudentGrades(@PathVariable UUID id) {
    return ResponseEntity.ok(gradeHistoryService.getGradesForStudent(id));
  }
}
