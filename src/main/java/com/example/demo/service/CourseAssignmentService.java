package com.example.demo.service;

import com.example.demo.domain.CourseAssignment;
import com.example.demo.exception.NotFoundException;
import com.example.demo.mapper.CourseAssignmentMapper;
import com.example.demo.model.CourseAssignmentDTO;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.CourseAssignmentRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseAssignmentService {

  private final CourseAssignmentRepository courseAssignmentRepository;
  private final CourseRepository courseRepository;
  private final UserRepository userRepository;
  private final AcademicYearRepository academicYearRepository;

  public List<CourseAssignmentDTO> listForCourse(UUID courseId) {
    return courseAssignmentRepository.findByCourseId(courseId).stream()
        .map(CourseAssignmentMapper::toDTO)
        .toList();
  }

  public CourseAssignmentDTO createAssignment(UUID courseId, UUID teacherId, UUID academicYearId) {
    var course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));
    var teacher =
        userRepository
            .findById(teacherId)
            .orElseThrow(() -> new NotFoundException("Teacher not found: " + teacherId));
    var academicYear =
        academicYearRepository
            .findById(academicYearId)
            .orElseThrow(() -> new NotFoundException("Academic year not found: " + academicYearId));

    var assignment = new CourseAssignment();
    assignment.setCourse(course);
    assignment.setTeacher(teacher);
    assignment.setAcademicYear(academicYear);

    return CourseAssignmentMapper.toDTO(courseAssignmentRepository.save(assignment));
  }

  public CourseAssignmentDTO updateTeacher(UUID assignmentId, UUID newTeacherId) {
    var assignment =
        courseAssignmentRepository
            .findById(assignmentId)
            .orElseThrow(
                () -> new NotFoundException("Course assignment not found: " + assignmentId));
    var teacher =
        userRepository
            .findById(newTeacherId)
            .orElseThrow(() -> new NotFoundException("Teacher not found: " + newTeacherId));

    assignment.setTeacher(teacher);

    return CourseAssignmentMapper.toDTO(courseAssignmentRepository.save(assignment));
  }
}
