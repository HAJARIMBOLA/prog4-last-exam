package com.example.demo.testdata;

import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Semester;
import java.time.LocalDate;
import java.util.UUID;

public class AcademicYearTestDataBuilder {

  private UUID id = UUID.randomUUID();
  private LocalDate startDate = LocalDate.of(2025, 9, 1);
  private LocalDate endDate = LocalDate.of(2026, 7, 1);

  public static AcademicYearTestDataBuilder anAcademicYear() {
    return new AcademicYearTestDataBuilder();
  }

  public AcademicYearTestDataBuilder withId(UUID id) {
    this.id = id;
    return this;
  }

  public AcademicYearTestDataBuilder withStartDate(LocalDate startDate) {
    this.startDate = startDate;
    return this;
  }

  public AcademicYearTestDataBuilder withEndDate(LocalDate endDate) {
    this.endDate = endDate;
    return this;
  }

  public AcademicYear build() {
    return new AcademicYear(id, startDate, endDate);
  }

  public Semester buildSemester(int semesterNumber) {
    var semester = new Semester();
    semester.setId(UUID.randomUUID());
    semester.setAcademicYear(build());
    semester.setSemesterNumber(semesterNumber);
    semester.setStartDate(startDate);
    semester.setEndDate(endDate);
    return semester;
  }
}
