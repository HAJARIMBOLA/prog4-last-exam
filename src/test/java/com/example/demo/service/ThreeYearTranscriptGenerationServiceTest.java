package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.StudentEnrollment;
import com.example.demo.domain.Track;
import com.example.demo.model.TranscriptDTO;
import com.example.demo.repository.StudentEnrollmentRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ThreeYearTranscriptGenerationServiceTest {

  @Mock private StudentEnrollmentRepository studentEnrollmentRepository;
  @Mock private TranscriptGenerationService transcriptGenerationService;
  @Mock private ThreeYearAverageService threeYearAverageService;

  @Test
  void aggregatesTheThreeNonRepeatingYearsInChronologicalOrder() {
    var studentId = UUID.randomUUID();
    var year1 = academicYear(LocalDate.of(2029, 9, 1));
    var year2 = academicYear(LocalDate.of(2030, 9, 1));
    var year3 = academicYear(LocalDate.of(2031, 9, 1));
    var repeatedAttempt = academicYear(LocalDate.of(2029, 9, 1));

    when(studentEnrollmentRepository.findByStudentId(studentId))
        .thenReturn(
            List.of(
                enrollment(year3, false),
                enrollment(repeatedAttempt, true),
                enrollment(year1, false),
                enrollment(year2, false)));

    var transcript1 = transcript(studentId, year1.getId());
    var transcript2 = transcript(studentId, year2.getId());
    var transcript3 = transcript(studentId, year3.getId());
    when(transcriptGenerationService.generate(studentId, year1.getId())).thenReturn(transcript1);
    when(transcriptGenerationService.generate(studentId, year2.getId())).thenReturn(transcript2);
    when(transcriptGenerationService.generate(studentId, year3.getId())).thenReturn(transcript3);
    when(threeYearAverageService.computeThreeYearAverage(
            studentId, List.of(year1.getId(), year2.getId(), year3.getId())))
        .thenReturn(Optional.of(13.5));

    var service =
        new ThreeYearTranscriptGenerationService(
            studentEnrollmentRepository, transcriptGenerationService, threeYearAverageService);

    var result = service.generate(studentId);

    assertThat(result.studentId()).isEqualTo(studentId);
    assertThat(result.yearlyTranscripts()).containsExactly(transcript1, transcript2, transcript3);
    assertThat(result.finalAverage()).isEqualTo(13.5);
  }

  @Test
  void resolveOrderedAcademicYearIdsExcludesRepeatingEnrollmentsAndCapsAtThree() {
    var studentId = UUID.randomUUID();
    var year1 = academicYear(LocalDate.of(2029, 9, 1));
    var year2 = academicYear(LocalDate.of(2030, 9, 1));
    var year3 = academicYear(LocalDate.of(2031, 9, 1));
    var year4 = academicYear(LocalDate.of(2032, 9, 1));

    when(studentEnrollmentRepository.findByStudentId(studentId))
        .thenReturn(
            List.of(
                enrollment(year1, false),
                enrollment(year2, false),
                enrollment(year3, false),
                enrollment(year4, false)));

    var service =
        new ThreeYearTranscriptGenerationService(
            studentEnrollmentRepository, transcriptGenerationService, threeYearAverageService);

    assertThat(service.resolveOrderedAcademicYearIds(studentId))
        .containsExactly(year1.getId(), year2.getId(), year3.getId());
  }

  private AcademicYear academicYear(LocalDate startDate) {
    var academicYear = new AcademicYear();
    academicYear.setId(UUID.randomUUID());
    academicYear.setStartDate(startDate);
    academicYear.setEndDate(startDate.plusMonths(10));
    return academicYear;
  }

  private StudentEnrollment enrollment(AcademicYear academicYear, boolean repeating) {
    var enrollment = new StudentEnrollment();
    enrollment.setAcademicYear(academicYear);
    enrollment.setTrackAtTime(Track.COMMON_CORE);
    enrollment.setRepeating(repeating);
    return enrollment;
  }

  private TranscriptDTO transcript(UUID studentId, UUID academicYearId) {
    return new TranscriptDTO(studentId, academicYearId, Instant.now(), List.of(), false, 14.0, 30);
  }
}
