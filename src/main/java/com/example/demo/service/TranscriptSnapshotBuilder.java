package com.example.demo.service;

import com.example.demo.model.GradeHistoryDTO;
import com.example.demo.model.TranscriptLineDTO;
import com.example.demo.model.TranscriptSnapshotDTO;
import com.example.demo.repository.ExamRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TranscriptSnapshotBuilder {

  private final StrictTrackFilterService strictTrackFilterService;
  private final ExamRepository examRepository;
  private final GradeHistoryService gradeHistoryService;

  public TranscriptSnapshotDTO build(UUID studentId, UUID academicYearId) {
    var courses = strictTrackFilterService.getCoursesForStudentAtYear(studentId, academicYearId);

    var lines =
        courses.stream()
            .flatMap(
                course ->
                    examRepository.findByCourseId(course.id()).stream()
                        .map(
                            exam -> {
                              var activeGrade =
                                  gradeHistoryService.getActiveGrade(studentId, exam.getId());
                              return new TranscriptLineDTO(
                                  course.id(),
                                  course.ref(),
                                  course.title(),
                                  exam.getId(),
                                  activeGrade.map(GradeHistoryDTO::value).orElse(null));
                            }))
            .toList();

    return new TranscriptSnapshotDTO(studentId, academicYearId, Instant.now(), lines);
  }
}
