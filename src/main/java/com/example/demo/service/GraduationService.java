package com.example.demo.service;

import com.example.demo.repository.StudentEnrollmentRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GraduationService {

  private static final double PASSING_AVERAGE = 10.0;

  private final StudentEnrollmentRepository studentEnrollmentRepository;
  private final StrictTrackFilterService strictTrackFilterService;
  private final CourseAverageService courseAverageService;

  public boolean isEligibleForGraduation(UUID studentId) {
    var finalEnrollments =
        studentEnrollmentRepository.findByStudentId(studentId).stream()
            .filter(enrollment -> !enrollment.isRepeating())
            .toList();

    if (finalEnrollments.isEmpty()) {
      return false;
    }

    return finalEnrollments.stream()
        .allMatch(
            enrollment -> allTrackCoursesPassed(studentId, enrollment.getAcademicYear().getId()));
  }

  private boolean allTrackCoursesPassed(UUID studentId, UUID academicYearId) {
    var courses = strictTrackFilterService.getCoursesForStudentAtYear(studentId, academicYearId);
    if (courses.isEmpty()) {
      return false;
    }

    return courses.stream()
        .allMatch(
            course ->
                courseAverageService
                    .computeCourseAverage(studentId, course.id())
                    .map(average -> average >= PASSING_AVERAGE)
                    .orElse(false));
  }
}
