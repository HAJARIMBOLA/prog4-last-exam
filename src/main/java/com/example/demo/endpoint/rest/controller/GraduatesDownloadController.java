package com.example.demo.endpoint.rest.controller;

import com.example.demo.domain.Track;
import com.example.demo.model.GraduatesDownloadResponse;
import com.example.demo.service.GraduatesXlsxPublishingService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class GraduatesDownloadController {

  private final GraduatesXlsxPublishingService graduatesXlsxPublishingService;

  @GetMapping("/promotions/{id}/graduates/download")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public ResponseEntity<GraduatesDownloadResponse> downloadGraduates(
      @PathVariable UUID id, @RequestParam Track track) {
    var url = graduatesXlsxPublishingService.publish(id, track);
    return ResponseEntity.ok(new GraduatesDownloadResponse(url.toString()));
  }
}
