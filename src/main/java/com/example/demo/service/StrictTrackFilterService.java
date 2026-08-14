package com.example.demo.service;

import com.example.demo.mapper.CourseMapper;
import com.example.demo.model.CourseDTO;
import com.example.demo.repository.CourseRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StrictTrackFilterService {

  private final StudentEnrollmentService studentEnrollmentService;
  private final CourseRepository courseRepository;
  private final CourseTrackValidationService courseTrackValidationService;

  public List<CourseDTO> getCoursesForStudentAtYear(UUID studentId, UUID academicYearId) {
    var track = studentEnrollmentService.resolveTrack(studentId, academicYearId);
    return courseRepository.findAll().stream()
        .filter(course -> courseTrackValidationService.courseMatchesTrack(course.getId(), track))
        .map(CourseMapper::toDTO)
        .toList();
  }
}
