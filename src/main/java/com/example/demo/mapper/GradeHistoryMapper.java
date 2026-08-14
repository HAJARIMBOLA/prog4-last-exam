package com.example.demo.mapper;

import com.example.demo.domain.GradeHistory;
import com.example.demo.model.GradeHistoryDTO;

public class GradeHistoryMapper {

  private GradeHistoryMapper() {}

  public static GradeHistoryDTO toDTO(GradeHistory gradeHistory) {
    return new GradeHistoryDTO(
        gradeHistory.getId(),
        gradeHistory.getStudent().getId(),
        gradeHistory.getExam().getId(),
        gradeHistory.getValue(),
        gradeHistory.getRecordedAt(),
        gradeHistory.getRecordedBy().getId());
  }
}
