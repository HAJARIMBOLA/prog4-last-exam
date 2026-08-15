package com.example.demo.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TranscriptDTO(
    UUID studentId,
    UUID academicYearId,
    Instant asOf,
    List<TranscriptLineDTO> lines,
    boolean provisional,
    Double generalAverage,
    int totalCredits) {

  public TranscriptDTO {
    lines = List.copyOf(lines);
  }
}
