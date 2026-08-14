package com.example.demo.mapper;

import com.example.demo.domain.Exam;
import com.example.demo.model.ExamDTO;

public class ExamMapper {

  private ExamMapper() {}

  public static ExamDTO toDTO(Exam exam) {
    return new ExamDTO(
        exam.getId(), exam.getDateExam(), exam.getCoefficient(), exam.getCourse().getId());
  }
}
