package com.example.demo.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Course;
import com.example.demo.domain.CourseTrack;
import com.example.demo.domain.Track;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CourseTrackMapperTest {

  @Test
  void mapsEntityToDTO() {
    var course = new Course(UUID.randomUUID(), "ALG101", "Algorithms", 4);
    var courseTrack = new CourseTrack(UUID.randomUUID(), course, Track.TN);

    var dto = CourseTrackMapper.toDTO(courseTrack);

    assertThat(dto.courseId()).isEqualTo(course.getId());
    assertThat(dto.track()).isEqualTo(Track.TN);
  }
}
