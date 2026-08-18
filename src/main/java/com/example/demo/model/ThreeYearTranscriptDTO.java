package com.example.demo.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ThreeYearTranscriptDTO(
    UUID studentId, List<TranscriptDTO> yearlyTranscripts, Double finalAverage, Instant asOf) {

  public ThreeYearTranscriptDTO {
    yearlyTranscripts = List.copyOf(yearlyTranscripts);
  }
}
