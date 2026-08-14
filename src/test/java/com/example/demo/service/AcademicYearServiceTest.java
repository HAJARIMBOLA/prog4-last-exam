package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Semester;
import com.example.demo.model.SemesterDTO;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.SemesterRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AcademicYearServiceTest {

  @Mock private AcademicYearRepository academicYearRepository;
  @Mock private SemesterRepository semesterRepository;

  @Test
  void academicYearAlwaysHasExactlyTwoSemesters() {
    var academicYearService = new AcademicYearService(academicYearRepository, semesterRepository);
    var academicYear =
        new AcademicYear(UUID.randomUUID(), LocalDate.of(2025, 9, 1), LocalDate.of(2026, 7, 1));
    when(academicYearRepository.save(any())).thenReturn(academicYear);
    when(semesterRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    var semesters =
        List.of(
            new SemesterDTO(null, null, 1, LocalDate.of(2025, 9, 1), LocalDate.of(2026, 1, 31)),
            new SemesterDTO(null, null, 2, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 7, 1)));

    var result =
        academicYearService.createAcademicYear(
            LocalDate.of(2025, 9, 1), LocalDate.of(2026, 7, 1), semesters);

    assertThat(result.semesters()).hasSize(2);
  }

  @Test
  void rejectsCreationWithThreeSemesters() {
    var academicYearService = new AcademicYearService(academicYearRepository, semesterRepository);
    var semesters =
        List.of(
            new SemesterDTO(null, null, 1, LocalDate.now(), LocalDate.now()),
            new SemesterDTO(null, null, 2, LocalDate.now(), LocalDate.now()),
            new SemesterDTO(null, null, 3, LocalDate.now(), LocalDate.now()));

    assertThatThrownBy(
            () ->
                academicYearService.createAcademicYear(LocalDate.now(), LocalDate.now(), semesters))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsCreationWithOnlyOneSemester() {
    var academicYearService = new AcademicYearService(academicYearRepository, semesterRepository);
    var semesters = List.of(new SemesterDTO(null, null, 1, LocalDate.now(), LocalDate.now()));

    assertThatThrownBy(
            () ->
                academicYearService.createAcademicYear(LocalDate.now(), LocalDate.now(), semesters))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void addSemesterRejectsAThirdSemester() {
    var academicYearService = new AcademicYearService(academicYearRepository, semesterRepository);
    var academicYearId = UUID.randomUUID();
    var academicYear = new AcademicYear(academicYearId, LocalDate.now(), LocalDate.now());
    when(academicYearRepository.findById(academicYearId)).thenReturn(Optional.of(academicYear));
    when(semesterRepository.findByAcademicYearId(academicYearId))
        .thenReturn(List.of(new Semester(), new Semester()));

    var semesterDTO = new SemesterDTO(null, academicYearId, 3, LocalDate.now(), LocalDate.now());

    assertThatThrownBy(() -> academicYearService.addSemester(academicYearId, semesterDTO))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
