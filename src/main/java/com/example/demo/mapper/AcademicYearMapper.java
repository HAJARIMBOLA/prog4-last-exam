package com.example.demo.mapper;

import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Semester;
import com.example.demo.model.AcademicYearDTO;
import com.example.demo.model.SemesterDTO;
import java.util.List;

public class AcademicYearMapper {

  private AcademicYearMapper() {}

  public static SemesterDTO toDTO(Semester semester) {
    return new SemesterDTO(
        semester.getId(),
        semester.getAcademicYear().getId(),
        semester.getSemesterNumber(),
        semester.getStartDate(),
        semester.getEndDate());
  }

  public static AcademicYearDTO toDTO(AcademicYear academicYear, List<Semester> semesters) {
    return new AcademicYearDTO(
        academicYear.getId(),
        academicYear.getStartDate(),
        academicYear.getEndDate(),
        semesters.stream().map(AcademicYearMapper::toDTO).toList());
  }
}
