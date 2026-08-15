package com.example.demo.service;

import com.example.demo.domain.GradeHistory;
import com.example.demo.mapper.GradeHistoryMapper;
import com.example.demo.model.GradeHistoryDTO;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.GradeHistoryRepository;
import com.example.demo.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GradeSubmissionService {

  private final GradeHistoryRepository gradeHistoryRepository;
  private final ExamRepository examRepository;
  private final UserRepository userRepository;

  public GradeHistoryDTO recordGrade(
      UUID examId, UUID studentId, double value, String recordedByEmail) {
    var exam =
        examRepository
            .findById(examId)
            .orElseThrow(() -> new IllegalArgumentException("Exam not found: " + examId));
    var student =
        userRepository
            .findById(studentId)
            .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
    var teacher =
        userRepository
            .findByEmail(recordedByEmail)
            .orElseThrow(
                () -> new IllegalArgumentException("Teacher not found: " + recordedByEmail));

    var gradeHistory = new GradeHistory();
    gradeHistory.setStudent(student);
    gradeHistory.setExam(exam);
    gradeHistory.setValue(value);
    gradeHistory.setRecordedAt(Instant.now());
    gradeHistory.setRecordedBy(teacher);

    return GradeHistoryMapper.toDTO(gradeHistoryRepository.save(gradeHistory));
  }
}
