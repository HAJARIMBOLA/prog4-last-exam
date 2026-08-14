package com.example.demo.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Promotion;
import com.example.demo.domain.Role;
import com.example.demo.domain.StudentEnrollment;
import com.example.demo.domain.Track;
import com.example.demo.domain.User;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StudentEnrollmentMapperTest {

  @Test
  void mapsEntityToDTO() {
    var student =
        new User(UUID.randomUUID(), "s@hei.school", "hash", Role.STUDENT, "STD001", "Jane", "Doe");
    var promotion = new Promotion(UUID.randomUUID(), "Promotion 2026", 2026);
    var academicYear = new AcademicYear(UUID.randomUUID(), LocalDate.now(), LocalDate.now());
    var enrollment =
        new StudentEnrollment(UUID.randomUUID(), student, promotion, academicYear, Track.EL, true);

    var dto = StudentEnrollmentMapper.toDTO(enrollment);

    assertThat(dto.trackAtTime()).isEqualTo(Track.EL);
    assertThat(dto.repeating()).isTrue();
  }
}
