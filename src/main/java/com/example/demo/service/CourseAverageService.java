package com.example.demo.service;

import com.example.demo.repository.ExamRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseAverageService {

  private final ExamRepository examRepository;
  private final GradeHistoryService gradeHistoryService;

  public Optional<Double> computeCourseAverage(UUID studentId, UUID courseId) {
    var exams = examRepository.findByCourseId(courseId);

    double weightedSum = 0.0;
    var anyGraded = false;
    for (var exam : exams) {
      var activeGrade = gradeHistoryService.getActiveGrade(studentId, exam.getId());
      if (activeGrade.isPresent()) {
        anyGraded = true;
        weightedSum += activeGrade.get().value() * exam.getCoefficient();
      }
    }

    return anyGraded ? Optional.of(weightedSum) : Optional.empty();
  }
}
