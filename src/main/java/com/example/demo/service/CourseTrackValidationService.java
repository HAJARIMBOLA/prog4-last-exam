package com.example.demo.service;

import com.example.demo.domain.CourseTrack;
import com.example.demo.domain.Track;
import com.example.demo.repository.CourseTrackRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseTrackValidationService {

  private final CourseTrackRepository courseTrackRepository;

  public boolean courseMatchesTrack(UUID courseId, Track studentTrack) {
    return courseTrackRepository.findByCourseId(courseId).stream()
        .map(CourseTrack::getTrack)
        .anyMatch(studentTrack::equals);
  }
}
