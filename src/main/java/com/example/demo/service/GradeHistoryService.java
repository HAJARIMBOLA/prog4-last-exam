package com.example.demo.service;

import com.example.demo.mapper.GradeHistoryMapper;
import com.example.demo.model.GradeHistoryDTO;
import com.example.demo.repository.GradeHistoryRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GradeHistoryService {

  private final GradeHistoryRepository gradeHistoryRepository;

  public Optional<GradeHistoryDTO> getActiveGrade(UUID studentId, UUID examId) {
    return gradeHistoryRepository
        .findByStudentIdAndExamIdOrderByRecordedAtDesc(studentId, examId)
        .stream()
        .findFirst()
        .map(GradeHistoryMapper::toDTO);
  }

  public List<GradeHistoryDTO> getFullHistory(UUID studentId, UUID examId) {
    return gradeHistoryRepository
        .findByStudentIdAndExamIdOrderByRecordedAtAsc(studentId, examId)
        .stream()
        .map(GradeHistoryMapper::toDTO)
        .toList();
  }

  public List<GradeHistoryDTO> getGradesForStudent(UUID studentId) {
    return gradeHistoryRepository.findByStudentId(studentId).stream()
        .map(GradeHistoryMapper::toDTO)
        .toList();
  }
}
