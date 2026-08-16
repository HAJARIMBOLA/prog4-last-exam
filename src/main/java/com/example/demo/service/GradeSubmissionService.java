package com.example.demo.service;

import com.example.demo.domain.Exam;
import com.example.demo.domain.GradeHistory;
import com.example.demo.exception.NotFoundException;
import com.example.demo.mapper.GradeHistoryMapper;
import com.example.demo.model.GradeHistoryDTO;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.CourseAssignmentRepository;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.GradeHistoryRepository;
import com.example.demo.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GradeSubmissionService {

  private final GradeHistoryRepository gradeHistoryRepository;
  private final ExamRepository examRepository;
  private final UserRepository userRepository;
  private final AcademicYearRepository academicYearRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;

  public GradeHistoryDTO recordGrade(
      UUID examId, UUID studentId, double value, String recordedByEmail) {
    var exam =
        examRepository
            .findById(examId)
            .orElseThrow(() -> new NotFoundException("Exam not found: " + examId));
    var student =
        userRepository
            .findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));
    var teacher =
        userRepository
            .findByEmail(recordedByEmail)
            .orElseThrow(() -> new NotFoundException("Teacher not found: " + recordedByEmail));

    if (!isTeacherAssignedToExam(teacher.getId(), exam)) {
      throw new AccessDeniedException(
          "Teacher "
              + recordedByEmail
              + " is not assigned to course "
              + exam.getCourse().getId()
              + " for the current academic year");
    }

    var gradeHistory = new GradeHistory();
    gradeHistory.setStudent(student);
    gradeHistory.setExam(exam);
    gradeHistory.setValue(value);
    gradeHistory.setRecordedAt(Instant.now());
    gradeHistory.setRecordedBy(teacher);

    return GradeHistoryMapper.toDTO(gradeHistoryRepository.save(gradeHistory));
  }

  private boolean isTeacherAssignedToExam(UUID teacherId, Exam exam) {
    var academicYear =
        academicYearRepository
            .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                exam.getDateExam(), exam.getDateExam())
            .orElseThrow(
                () ->
                    new NotFoundException(
                        "No academic year found for exam date: " + exam.getDateExam()));

    return courseAssignmentRepository
        .findByCourseIdAndAcademicYearId(exam.getCourse().getId(), academicYear.getId())
        .stream()
        .anyMatch(assignment -> assignment.getTeacher().getId().equals(teacherId));
  }
}
