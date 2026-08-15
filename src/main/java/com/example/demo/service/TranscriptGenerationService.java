package com.example.demo.service;

import com.example.demo.model.CourseDTO;
import com.example.demo.model.TranscriptDTO;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TranscriptGenerationService {

  private final TranscriptSnapshotBuilder transcriptSnapshotBuilder;
  private final StrictTrackFilterService strictTrackFilterService;
  private final ExamCreationService examCreationService;
  private final YearAverageService yearAverageService;

  public TranscriptDTO generate(UUID studentId, UUID academicYearId) {
    var snapshot = transcriptSnapshotBuilder.build(studentId, academicYearId);
    var courses = strictTrackFilterService.getCoursesForStudentAtYear(studentId, academicYearId);

    var provisional =
        courses.stream().anyMatch(course -> !examCreationService.isCourseComplete(course.id()));
    var totalCredits = courses.stream().mapToInt(CourseDTO::credits).sum();
    var generalAverage =
        yearAverageService.computeYearAverage(studentId, academicYearId).orElse(null);

    return new TranscriptDTO(
        snapshot.studentId(),
        snapshot.academicYearId(),
        snapshot.asOf(),
        snapshot.lines(),
        provisional,
        generalAverage,
        totalCredits);
  }
}
