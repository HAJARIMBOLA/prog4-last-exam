package com.example.demo.service;

import com.example.demo.mapper.StudentEnrollmentMapper;
import com.example.demo.model.CourseDTO;
import com.example.demo.model.StudentEnrollmentDTO;
import com.example.demo.repository.StudentEnrollmentRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class YearRepetitionService {

  private static final int REQUIRED_CREDITS_PER_YEAR = 60;
  private static final double PASSING_AVERAGE = 10.0;

  private final StrictTrackFilterService strictTrackFilterService;
  private final CourseAverageService courseAverageService;
  private final YearAverageService yearAverageService;
  private final StudentEnrollmentRepository studentEnrollmentRepository;

  public StudentEnrollmentDTO evaluateAndUpdateRepetition(UUID studentId, UUID academicYearId) {
    var enrollment =
        studentEnrollmentRepository
            .findByStudentIdAndAcademicYearId(studentId, academicYearId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "No enrollment found for student "
                            + studentId
                            + " in academic year "
                            + academicYearId));

    var earnedCredits = computeEarnedCredits(studentId, academicYearId);
    var yearAverage = yearAverageService.computeYearAverage(studentId, academicYearId).orElse(0.0);

    var repeats = earnedCredits < REQUIRED_CREDITS_PER_YEAR || yearAverage < PASSING_AVERAGE;
    enrollment.setRepeating(repeats);

    return StudentEnrollmentMapper.toDTO(studentEnrollmentRepository.save(enrollment));
  }

  private int computeEarnedCredits(UUID studentId, UUID academicYearId) {
    return strictTrackFilterService.getCoursesForStudentAtYear(studentId, academicYearId).stream()
        .filter(course -> hasPassed(studentId, course))
        .mapToInt(CourseDTO::credits)
        .sum();
  }

  private boolean hasPassed(UUID studentId, CourseDTO course) {
    return courseAverageService
        .computeCourseAverage(studentId, course.id())
        .map(average -> average >= PASSING_AVERAGE)
        .orElse(false);
  }
}
