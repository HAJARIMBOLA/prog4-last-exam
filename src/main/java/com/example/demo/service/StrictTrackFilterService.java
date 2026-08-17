package com.example.demo.service;

import com.example.demo.domain.CourseTrack;
import com.example.demo.domain.Semester;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.model.CourseDTO;
import com.example.demo.repository.CourseTrackRepository;
import com.example.demo.repository.SemesterRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StrictTrackFilterService {

  private final StudentEnrollmentService studentEnrollmentService;
  private final SemesterRepository semesterRepository;
  private final CourseTrackRepository courseTrackRepository;

  public List<CourseDTO> getCoursesForStudentAtYear(UUID studentId, UUID academicYearId) {
    var track = studentEnrollmentService.resolveTrack(studentId, academicYearId);
    var semesterIds =
        semesterRepository.findByAcademicYearId(academicYearId).stream()
            .map(Semester::getId)
            .toList();

    return semesterIds.stream()
        .flatMap(
            semesterId ->
                courseTrackRepository.findByTrackAndSemesterId(track, semesterId).stream())
        .map(CourseTrack::getCourse)
        .distinct()
        .map(CourseMapper::toDTO)
        .toList();
  }
}
