package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Promotion;
import com.example.demo.domain.Role;
import com.example.demo.domain.StudentEnrollment;
import com.example.demo.domain.Track;
import com.example.demo.domain.User;
import com.example.demo.repository.StudentEnrollmentRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentEnrollmentServiceTest {

  @Mock private StudentEnrollmentRepository studentEnrollmentRepository;

  @Test
  void yearOneResolvesToCommonCore() {
    var studentId = UUID.randomUUID();
    var academicYearId = UUID.randomUUID();
    when(studentEnrollmentRepository.findByStudentIdAndAcademicYearId(studentId, academicYearId))
        .thenReturn(Optional.of(enrollmentWith(Track.COMMON_CORE, false)));

    var service = new StudentEnrollmentService(studentEnrollmentRepository);

    assertThat(service.resolveTrack(studentId, academicYearId)).isEqualTo(Track.COMMON_CORE);
  }

  @Test
  void laterYearsResolveToELOrTNPerEnrollment() {
    var studentId = UUID.randomUUID();
    var elYearId = UUID.randomUUID();
    var tnYearId = UUID.randomUUID();
    when(studentEnrollmentRepository.findByStudentIdAndAcademicYearId(studentId, elYearId))
        .thenReturn(Optional.of(enrollmentWith(Track.EL, false)));
    when(studentEnrollmentRepository.findByStudentIdAndAcademicYearId(studentId, tnYearId))
        .thenReturn(Optional.of(enrollmentWith(Track.TN, false)));

    var service = new StudentEnrollmentService(studentEnrollmentRepository);

    assertThat(service.resolveTrack(studentId, elYearId)).isEqualTo(Track.EL);
    assertThat(service.resolveTrack(studentId, tnYearId)).isEqualTo(Track.TN);
  }

  @Test
  void repeatingFlagIsCorrectlyRead() {
    var studentId = UUID.randomUUID();
    var academicYearId = UUID.randomUUID();
    when(studentEnrollmentRepository.findByStudentIdAndAcademicYearId(studentId, academicYearId))
        .thenReturn(Optional.of(enrollmentWith(Track.TN, true)));

    var service = new StudentEnrollmentService(studentEnrollmentRepository);
    var enrollment = service.resolveEnrollment(studentId, academicYearId);

    assertThat(enrollment).isPresent();
    assertThat(enrollment.get().repeating()).isTrue();
  }

  private StudentEnrollment enrollmentWith(Track track, boolean repeating) {
    var student =
        new User(UUID.randomUUID(), "s@hei.school", "hash", Role.STUDENT, "STD001", "Jane", "Doe");
    var promotion = new Promotion(UUID.randomUUID(), "Promotion 2026", 2026);
    var academicYear = new AcademicYear(UUID.randomUUID(), LocalDate.now(), LocalDate.now());
    return new StudentEnrollment(
        UUID.randomUUID(), student, promotion, academicYear, track, repeating);
  }
}
