package com.example.demo.mapper;

import com.example.demo.domain.StudentEnrollment;
import com.example.demo.model.StudentEnrollmentDTO;

public class StudentEnrollmentMapper {

  private StudentEnrollmentMapper() {}

  public static StudentEnrollmentDTO toDTO(StudentEnrollment enrollment) {
    return new StudentEnrollmentDTO(
        enrollment.getId(),
        enrollment.getStudent().getId(),
        enrollment.getPromotion().getId(),
        enrollment.getAcademicYear().getId(),
        enrollment.getTrackAtTime(),
        enrollment.isRepeating());
  }
}
