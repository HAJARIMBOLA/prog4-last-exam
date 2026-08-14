package com.example.demo.mapper;

import com.example.demo.domain.Course;
import com.example.demo.model.CourseDTO;

public class CourseMapper {

  private CourseMapper() {}

  public static CourseDTO toDTO(Course course) {
    return new CourseDTO(course.getId(), course.getRef(), course.getTitle(), course.getCredits());
  }
}
