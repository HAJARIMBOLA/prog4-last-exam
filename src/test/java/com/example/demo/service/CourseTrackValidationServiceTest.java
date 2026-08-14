package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Course;
import com.example.demo.domain.CourseTrack;
import com.example.demo.domain.Track;
import com.example.demo.repository.CourseTrackRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseTrackValidationServiceTest {

  @Mock private CourseTrackRepository courseTrackRepository;

  @Test
  void tnOnlyCourseNeverMatchesEL() {
    var courseId = UUID.randomUUID();
    when(courseTrackRepository.findByCourseId(courseId))
        .thenReturn(List.of(courseTrackWith(courseId, Track.TN)));
    var service = new CourseTrackValidationService(courseTrackRepository);

    assertThat(service.courseMatchesTrack(courseId, Track.TN)).isTrue();
    assertThat(service.courseMatchesTrack(courseId, Track.EL)).isFalse();
  }

  @Test
  void elOnlyCourseNeverMatchesTN() {
    var courseId = UUID.randomUUID();
    when(courseTrackRepository.findByCourseId(courseId))
        .thenReturn(List.of(courseTrackWith(courseId, Track.EL)));
    var service = new CourseTrackValidationService(courseTrackRepository);

    assertThat(service.courseMatchesTrack(courseId, Track.EL)).isTrue();
    assertThat(service.courseMatchesTrack(courseId, Track.TN)).isFalse();
  }

  @Test
  void sharedCourseMatchesBothTracks() {
    var courseId = UUID.randomUUID();
    when(courseTrackRepository.findByCourseId(courseId))
        .thenReturn(
            List.of(courseTrackWith(courseId, Track.EL), courseTrackWith(courseId, Track.TN)));
    var service = new CourseTrackValidationService(courseTrackRepository);

    assertThat(service.courseMatchesTrack(courseId, Track.EL)).isTrue();
    assertThat(service.courseMatchesTrack(courseId, Track.TN)).isTrue();
  }

  private CourseTrack courseTrackWith(UUID courseId, Track track) {
    var course = new Course();
    course.setId(courseId);
    var courseTrack = new CourseTrack();
    courseTrack.setCourse(course);
    courseTrack.setTrack(track);
    return courseTrack;
  }
}
