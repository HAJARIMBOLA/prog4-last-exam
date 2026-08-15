package com.example.demo.service;

import com.example.demo.repository.StudentEnrollmentRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AcademicYearClosingService {

  private final StudentEnrollmentRepository studentEnrollmentRepository;
  private final YearRepetitionService yearRepetitionService;
  private final TranscriptGenerationService transcriptGenerationService;

  public List<UUID> closeAcademicYear(UUID academicYearId) {
    var studentIds =
        studentEnrollmentRepository.findByAcademicYearId(academicYearId).stream()
            .map(enrollment -> enrollment.getStudent().getId())
            .toList();

    studentIds.forEach(
        studentId -> {
          yearRepetitionService.evaluateAndUpdateRepetition(studentId, academicYearId);
          transcriptGenerationService.generate(studentId, academicYearId);
        });

    return studentIds;
  }
}
