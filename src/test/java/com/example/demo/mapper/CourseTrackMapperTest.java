package com.example.demo.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Course;
import com.example.demo.domain.CourseTrack;
import com.example.demo.domain.Semester;
import com.example.demo.domain.Track;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CourseTrackMapperTest {

  @Test
  void mapsEntityToDTO() {
    var course = new Course(UUID.randomUUID(), "ALG101", "Algorithms", 4);
    var academicYear = new AcademicYear(UUID.randomUUID(), LocalDate.now(), LocalDate.now());
    var semester =
        new Semester(UUID.randomUUID(), academicYear, 1, LocalDate.now(), LocalDate.now());
    var courseTrack = new CourseTrack(UUID.randomUUID(), course, Track.TN, semester);

    var dto = CourseTrackMapper.toDTO(courseTrack);

    assertThat(dto.courseId()).isEqualTo(course.getId());
    assertThat(dto.track()).isEqualTo(Track.TN);
    assertThat(dto.semesterId()).isEqualTo(semester.getId());
  }
}
