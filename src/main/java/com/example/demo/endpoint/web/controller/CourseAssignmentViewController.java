package com.example.demo.endpoint.web.controller;

import com.example.demo.domain.Role;
import com.example.demo.exception.NotFoundException;
import com.example.demo.mapper.AcademicYearMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CourseAssignmentService;
import com.example.demo.service.CourseService;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class CourseAssignmentViewController {

  private final CourseAssignmentService courseAssignmentService;
  private final CourseService courseService;
  private final UserRepository userRepository;
  private final AcademicYearRepository academicYearRepository;

  @GetMapping("/ui/courses/{courseId}/assignments")
  @PreAuthorize("hasRole('ADMIN')")
  public String listAssignments(@PathVariable UUID courseId, Model model) {
    populateModel(courseId, model);
    return "course-assignments";
  }

  @PostMapping("/ui/courses/{courseId}/assignments")
  @PreAuthorize("hasRole('ADMIN')")
  public String createAssignment(
      @PathVariable UUID courseId,
      @RequestParam UUID teacherId,
      @RequestParam UUID academicYearId) {
    courseAssignmentService.createAssignment(courseId, teacherId, academicYearId);
    return "redirect:/ui/courses/{courseId}/assignments";
  }

  @PostMapping("/ui/courses/{courseId}/assignments/{assignmentId}")
  @PreAuthorize("hasRole('ADMIN')")
  public String updateAssignment(
      @PathVariable UUID courseId, @PathVariable UUID assignmentId, @RequestParam UUID teacherId) {
    courseAssignmentService.updateTeacher(assignmentId, teacherId);
    return "redirect:/ui/courses/{courseId}/assignments";
  }

  private void populateModel(UUID courseId, Model model) {
    var course =
        courseService
            .findCourse(courseId)
            .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));

    model.addAttribute("course", course);
    model.addAttribute("assignments", courseAssignmentService.listForCourse(courseId));
    model.addAttribute(
        "teachers",
        userRepository.findByRole(Role.TEACHER).stream().map(UserMapper::toDTO).toList());
    model.addAttribute(
        "academicYears",
        academicYearRepository.findAll().stream()
            .map(academicYear -> AcademicYearMapper.toDTO(academicYear, List.of()))
            .toList());
  }
}
