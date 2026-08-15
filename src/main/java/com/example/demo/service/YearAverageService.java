package com.example.demo.service;

import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class YearAverageService {

  private final StrictTrackFilterService strictTrackFilterService;
  private final CourseAverageService courseAverageService;

  public Optional<Double> computeYearAverage(UUID studentId, UUID academicYearId) {
    var courses = strictTrackFilterService.getCoursesForStudentAtYear(studentId, academicYearId);

    double weightedSum = 0.0;
    var totalCredits = 0;
    for (var course : courses) {
      var courseAverage = courseAverageService.computeCourseAverage(studentId, course.id());
      if (courseAverage.isPresent()) {
        weightedSum += courseAverage.get() * course.credits();
        totalCredits += course.credits();
      }
    }

    return totalCredits > 0 ? Optional.of(weightedSum / totalCredits) : Optional.empty();
  }
}
