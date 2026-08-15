package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Promotion;
import com.example.demo.domain.Role;
import com.example.demo.domain.StudentEnrollment;
import com.example.demo.domain.Track;
import com.example.demo.domain.User;
import com.example.demo.repository.StudentEnrollmentRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AcademicYearClosingServiceTest {

  @Mock private StudentEnrollmentRepository studentEnrollmentRepository;
  @Mock private YearRepetitionService yearRepetitionService;
  @Mock private TranscriptGenerationService transcriptGenerationService;

  @Test
  void repetitionIsEvaluatedBeforeTranscriptGenerationForEveryEnrolledStudent() {
    var academicYearId = UUID.randomUUID();
    var student1 = UUID.randomUUID();
    var student2 = UUID.randomUUID();
    when(studentEnrollmentRepository.findByAcademicYearId(academicYearId))
        .thenReturn(
            List.of(
                enrollmentOf(student1, academicYearId), enrollmentOf(student2, academicYearId)));

    var service =
        new AcademicYearClosingService(
            studentEnrollmentRepository, yearRepetitionService, transcriptGenerationService);
    var processed = service.closeAcademicYear(academicYearId);

    assertThat(processed).containsExactly(student1, student2);

    var order = inOrder(yearRepetitionService, transcriptGenerationService);
    order.verify(yearRepetitionService).evaluateAndUpdateRepetition(student1, academicYearId);
    order.verify(transcriptGenerationService).generate(student1, academicYearId);
    order.verify(yearRepetitionService).evaluateAndUpdateRepetition(student2, academicYearId);
    order.verify(transcriptGenerationService).generate(student2, academicYearId);
  }

  private StudentEnrollment enrollmentOf(UUID studentId, UUID academicYearId) {
    var student =
        new User(studentId, "s@hei.school", "hash", Role.STUDENT, "STD001", "Jane", "Doe");
    var promotion = new Promotion(UUID.randomUUID(), "P2026", 2029);
    var academicYear = new AcademicYear(academicYearId, LocalDate.now(), LocalDate.now());
    return new StudentEnrollment(
        UUID.randomUUID(), student, promotion, academicYear, Track.EL, false);
  }
}
