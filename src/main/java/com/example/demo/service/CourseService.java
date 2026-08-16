package com.example.demo.service;

import com.example.demo.domain.CourseAssignment;
import com.example.demo.domain.CourseTrack;
import com.example.demo.exception.NotFoundException;
import com.example.demo.mapper.CourseMapper;
import com.example.demo.model.CourseCreationRequest;
import com.example.demo.model.CourseDTO;
import com.example.demo.model.PageResponseDTO;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.CourseAssignmentRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.CourseTrackRepository;
import com.example.demo.repository.SemesterRepository;
import com.example.demo.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseService {

  private final CourseRepository courseRepository;
  private final CourseTrackRepository courseTrackRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final UserRepository userRepository;
  private final AcademicYearRepository academicYearRepository;
  private final SemesterRepository semesterRepository;
  private final CreditStructureValidationService creditStructureValidationService;

  public CourseDTO createCourse(CourseCreationRequest request) {
    var course = CourseMapper.toEntity(request);
    var savedCourse = courseRepository.save(course);

    var semester =
        semesterRepository
            .findById(request.semesterId())
            .orElseThrow(
                () -> new NotFoundException("Semester not found: " + request.semesterId()));

    request
        .tracks()
        .forEach(
            track -> {
              creditStructureValidationService.assertAddingCourseWithinSemesterCap(
                  track, semester.getId(), savedCourse.getCredits());
              var courseTrack = new CourseTrack();
              courseTrack.setCourse(savedCourse);
              courseTrack.setTrack(track);
              courseTrack.setSemester(semester);
              courseTrackRepository.save(courseTrack);
            });

    var academicYear =
        academicYearRepository
            .findById(request.academicYearId())
            .orElseThrow(
                () ->
                    new NotFoundException("Academic year not found: " + request.academicYearId()));

    request
        .teacherIds()
        .forEach(
            teacherId -> {
              var teacher =
                  userRepository
                      .findById(teacherId)
                      .orElseThrow(() -> new NotFoundException("Teacher not found: " + teacherId));
              var assignment = new CourseAssignment();
              assignment.setCourse(savedCourse);
              assignment.setTeacher(teacher);
              assignment.setAcademicYear(academicYear);
              courseAssignmentRepository.save(assignment);
            });

    return CourseMapper.toDTO(savedCourse);
  }

  public PageResponseDTO<CourseDTO> listCourses(Pageable pageable) {
    var page = courseRepository.findAll(pageable).map(CourseMapper::toDTO);
    return new PageResponseDTO<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages());
  }

  public Optional<CourseDTO> findCourse(UUID id) {
    return courseRepository.findById(id).map(CourseMapper::toDTO);
  }
}
