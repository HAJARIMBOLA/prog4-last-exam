package com.example.demo.service;

import com.example.demo.domain.Exam;
import com.example.demo.exception.InvalidCoefficientSumException;
import com.example.demo.mapper.ExamMapper;
import com.example.demo.model.ExamCreationRequest;
import com.example.demo.model.ExamDTO;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.ExamRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ExamCreationService {

  private static final double COMPLETE_COEFFICIENT_SUM = 1.0;
  private static final double COEFFICIENT_EPSILON = 1e-9;

  private final ExamRepository examRepository;
  private final CourseRepository courseRepository;

  public ExamDTO createExam(UUID courseId, ExamCreationRequest request) {
    var course =
        courseRepository
            .findById(courseId)
            .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

    var existingCoefficientSum = sumCoefficients(courseId);
    if (existingCoefficientSum + request.coefficient()
        > COMPLETE_COEFFICIENT_SUM + COEFFICIENT_EPSILON) {
      throw new InvalidCoefficientSumException(
          "Adding an exam with coefficient "
              + request.coefficient()
              + " to course "
              + courseId
              + " would bring the coefficient sum to "
              + (existingCoefficientSum + request.coefficient())
              + ", above the required "
              + COMPLETE_COEFFICIENT_SUM);
    }

    var exam = new Exam();
    exam.setDateExam(request.dateExam());
    exam.setCoefficient(request.coefficient());
    exam.setCourse(course);

    return ExamMapper.toDTO(examRepository.save(exam));
  }

  public boolean isCourseComplete(UUID courseId) {
    return Math.abs(sumCoefficients(courseId) - COMPLETE_COEFFICIENT_SUM) < COEFFICIENT_EPSILON;
  }

  private double sumCoefficients(UUID courseId) {
    return examRepository.findByCourseId(courseId).stream().mapToDouble(Exam::getCoefficient).sum();
  }
}
