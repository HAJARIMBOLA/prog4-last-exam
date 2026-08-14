package com.example.demo.service;

import com.example.demo.domain.CourseAssignment;
import com.example.demo.domain.CourseTrack;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.model.CourseCreationRequest;
import com.example.demo.model.CourseDTO;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.CourseAssignmentRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.CourseTrackRepository;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseService {

  private final CourseRepository courseRepository;
  private final CourseTrackRepository courseTrackRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final UserRepository userRepository;
  private final AcademicYearRepository academicYearRepository;

  public CourseDTO createCourse(CourseCreationRequest request) {
    var course = CourseMapper.toEntity(request);
    var savedCourse = courseRepository.save(course);

    request
        .tracks()
        .forEach(
            track -> {
              var courseTrack = new CourseTrack();
              courseTrack.setCourse(savedCourse);
              courseTrack.setTrack(track);
              courseTrackRepository.save(courseTrack);
            });

    var academicYear =
        academicYearRepository
            .findById(request.academicYearId())
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Academic year not found: " + request.academicYearId()));

    request
        .teacherIds()
        .forEach(
            teacherId -> {
              var teacher =
                  userRepository
                      .findById(teacherId)
                      .orElseThrow(
                          () -> new IllegalArgumentException("Teacher not found: " + teacherId));
              var assignment = new CourseAssignment();
              assignment.setCourse(savedCourse);
              assignment.setTeacher(teacher);
              assignment.setAcademicYear(academicYear);
              courseAssignmentRepository.save(assignment);
            });

    return CourseMapper.toDTO(savedCourse);
  }

  public List<CourseDTO> listCourses() {
    return courseRepository.findAll().stream().map(CourseMapper::toDTO).toList();
  }

  public Optional<CourseDTO> findCourse(UUID id) {
    return courseRepository.findById(id).map(CourseMapper::toDTO);
  }
}
