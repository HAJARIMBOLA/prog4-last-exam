package com.example.demo.service;

import com.example.demo.model.ThreeYearTranscriptDTO;
import com.example.demo.repository.StudentEnrollmentRepository;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ThreeYearTranscriptGenerationService {

  private static final int MAX_YEARS = 3;

  private final StudentEnrollmentRepository studentEnrollmentRepository;
  private final TranscriptGenerationService transcriptGenerationService;
  private final ThreeYearAverageService threeYearAverageService;

  @Transactional(readOnly = true)
  public List<UUID> resolveOrderedAcademicYearIds(UUID studentId) {
    return studentEnrollmentRepository.findByStudentId(studentId).stream()
        .filter(enrollment -> !enrollment.isRepeating())
        .sorted(Comparator.comparing(enrollment -> enrollment.getAcademicYear().getStartDate()))
        .map(enrollment -> enrollment.getAcademicYear().getId())
        .limit(MAX_YEARS)
        .toList();
  }

  @Transactional(readOnly = true)
  public ThreeYearTranscriptDTO generate(UUID studentId) {
    var academicYearIds = resolveOrderedAcademicYearIds(studentId);

    var yearlyTranscripts =
        academicYearIds.stream()
            .map(academicYearId -> transcriptGenerationService.generate(studentId, academicYearId))
            .toList();

    var finalAverage =
        threeYearAverageService.computeThreeYearAverage(studentId, academicYearIds).orElse(null);

    return new ThreeYearTranscriptDTO(studentId, yearlyTranscripts, finalAverage, Instant.now());
  }
}
