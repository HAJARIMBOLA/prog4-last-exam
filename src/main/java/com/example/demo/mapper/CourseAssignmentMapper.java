package com.example.demo.mapper;

import com.example.demo.domain.CourseAssignment;
import com.example.demo.model.CourseAssignmentDTO;

public class CourseAssignmentMapper {

  private CourseAssignmentMapper() {}

  public static CourseAssignmentDTO toDTO(CourseAssignment courseAssignment) {
    return new CourseAssignmentDTO(
        courseAssignment.getId(),
        courseAssignment.getCourse().getId(),
        courseAssignment.getTeacher().getId(),
        courseAssignment.getAcademicYear().getId());
  }
}
