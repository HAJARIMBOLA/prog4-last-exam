package com.example.demo.endpoint.rest.controller;

import com.example.demo.model.AcademicYearClosingResponse;
import com.example.demo.service.AcademicYearClosingService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AcademicYearClosingController {

  private final AcademicYearClosingService academicYearClosingService;

  @PostMapping("/academic-years/{id}/close")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<AcademicYearClosingResponse> closeAcademicYear(@PathVariable UUID id) {
    var processedStudentIds = academicYearClosingService.closeAcademicYear(id);
    return ResponseEntity.ok(new AcademicYearClosingResponse(processedStudentIds.size()));
  }
}
