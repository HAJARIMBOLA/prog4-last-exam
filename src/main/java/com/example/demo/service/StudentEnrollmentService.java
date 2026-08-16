package com.example.demo.service;

import com.example.demo.domain.StudentEnrollment;
import com.example.demo.domain.Track;
import com.example.demo.exception.StudentNotEnrolledException;
import com.example.demo.mapper.StudentEnrollmentMapper;
import com.example.demo.model.StudentEnrollmentDTO;
import com.example.demo.repository.StudentEnrollmentRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StudentEnrollmentService {

  private final StudentEnrollmentRepository studentEnrollmentRepository;

  public Optional<StudentEnrollmentDTO> resolveEnrollment(UUID studentId, UUID academicYearId) {
    return studentEnrollmentRepository
        .findByStudentIdAndAcademicYearId(studentId, academicYearId)
        .map(StudentEnrollmentMapper::toDTO);
  }

  public Track resolveTrack(UUID studentId, UUID academicYearId) {
    return studentEnrollmentRepository
        .findByStudentIdAndAcademicYearId(studentId, academicYearId)
        .map(StudentEnrollment::getTrackAtTime)
        .orElseThrow(
            () ->
                new StudentNotEnrolledException(
                    "No enrollment found for student "
                        + studentId
                        + " in academic year "
                        + academicYearId));
  }
}
