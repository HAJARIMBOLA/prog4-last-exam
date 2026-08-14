package com.example.demo.mapper;

import com.example.demo.domain.Course;
import com.example.demo.model.CourseCreationRequest;
import com.example.demo.model.CourseDTO;

public class CourseMapper {

  private CourseMapper() {}

  public static CourseDTO toDTO(Course course) {
    return new CourseDTO(course.getId(), course.getRef(), course.getTitle(), course.getCredits());
  }

  public static Course toEntity(CourseCreationRequest request) {
    var course = new Course();
    course.setRef(request.ref());
    course.setTitle(request.title());
    course.setCredits(request.credits());
    return course;
  }
}
