package com.example.demo.service;

import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Semester;
import com.example.demo.mapper.AcademicYearMapper;
import com.example.demo.model.AcademicYearDTO;
import com.example.demo.model.SemesterDTO;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.SemesterRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AcademicYearService {

  private static final int REQUIRED_SEMESTER_COUNT = 2;

  private final AcademicYearRepository academicYearRepository;
  private final SemesterRepository semesterRepository;

  public AcademicYearDTO createAcademicYear(
      LocalDate startDate, LocalDate endDate, List<SemesterDTO> semesters) {
    if (semesters.size() != REQUIRED_SEMESTER_COUNT) {
      throw new IllegalArgumentException(
          "An academic year must have exactly " + REQUIRED_SEMESTER_COUNT + " semesters");
    }

    var academicYear = new AcademicYear();
    academicYear.setStartDate(startDate);
    academicYear.setEndDate(endDate);
    var savedAcademicYear = academicYearRepository.save(academicYear);

    var savedSemesters =
        semesters.stream()
            .map(
                semesterDTO -> {
                  var semester = new Semester();
                  semester.setAcademicYear(savedAcademicYear);
                  semester.setSemesterNumber(semesterDTO.semesterNumber());
                  semester.setStartDate(semesterDTO.startDate());
                  semester.setEndDate(semesterDTO.endDate());
                  return semesterRepository.save(semester);
                })
            .toList();

    return AcademicYearMapper.toDTO(savedAcademicYear, savedSemesters);
  }

  public SemesterDTO addSemester(UUID academicYearId, SemesterDTO semesterDTO) {
    var academicYear =
        academicYearRepository
            .findById(academicYearId)
            .orElseThrow(
                () -> new IllegalArgumentException("Academic year not found: " + academicYearId));

    var existingSemesters = semesterRepository.findByAcademicYearId(academicYearId);
    if (existingSemesters.size() >= REQUIRED_SEMESTER_COUNT) {
      throw new IllegalArgumentException(
          "An academic year cannot have more than " + REQUIRED_SEMESTER_COUNT + " semesters");
    }

    var semester = new Semester();
    semester.setAcademicYear(academicYear);
    semester.setSemesterNumber(semesterDTO.semesterNumber());
    semester.setStartDate(semesterDTO.startDate());
    semester.setEndDate(semesterDTO.endDate());

    return AcademicYearMapper.toDTO(semesterRepository.save(semester));
  }
}
