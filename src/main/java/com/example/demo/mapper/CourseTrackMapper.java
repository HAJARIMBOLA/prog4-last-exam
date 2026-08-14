package com.example.demo.mapper;

import com.example.demo.domain.CourseTrack;
import com.example.demo.model.CourseTrackDTO;

public class CourseTrackMapper {

  private CourseTrackMapper() {}

  public static CourseTrackDTO toDTO(CourseTrack courseTrack) {
    return new CourseTrackDTO(
        courseTrack.getId(), courseTrack.getCourse().getId(), courseTrack.getTrack());
  }
}
