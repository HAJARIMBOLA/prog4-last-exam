package com.example.demo.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TranscriptSnapshotDTO(
    UUID studentId, UUID academicYearId, Instant asOf, List<TranscriptLineDTO> lines) {

  public TranscriptSnapshotDTO {
    lines = List.copyOf(lines);
  }
}
