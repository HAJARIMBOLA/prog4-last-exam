package com.example.demo.service;

import com.example.demo.domain.Track;
import com.example.demo.model.RankingEntryDTO;
import com.example.demo.repository.StudentEnrollmentRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class RankingService {

  private final StudentEnrollmentRepository studentEnrollmentRepository;
  private final ThreeYearAverageService threeYearAverageService;

  public List<RankingEntryDTO> rankPromotionByTrack(UUID promotionId, Track track) {
    var enrollmentsByStudent =
        studentEnrollmentRepository.findByPromotionId(promotionId).stream()
            .collect(Collectors.groupingBy(enrollment -> enrollment.getStudent().getId()));

    var studentAcademicYearIds = new LinkedHashMap<UUID, List<UUID>>();
    enrollmentsByStudent.forEach(
        (studentId, enrollments) -> {
          var matchesTrack =
              enrollments.stream().anyMatch(enrollment -> enrollment.getTrackAtTime() == track);
          if (matchesTrack) {
            var academicYearIds =
                enrollments.stream()
                    .filter(enrollment -> !enrollment.isRepeating())
                    .map(enrollment -> enrollment.getAcademicYear().getId())
                    .toList();
            studentAcademicYearIds.put(studentId, academicYearIds);
          }
        });

    return rankStudents(studentAcademicYearIds);
  }

  public List<RankingEntryDTO> rankStudents(Map<UUID, List<UUID>> studentAcademicYearIds) {
    var averages =
        studentAcademicYearIds.entrySet().stream()
            .map(
                entry ->
                    Map.entry(
                        entry.getKey(),
                        threeYearAverageService.computeThreeYearAverage(
                            entry.getKey(), entry.getValue())))
            .filter(entry -> entry.getValue().isPresent())
            .map(entry -> Map.entry(entry.getKey(), entry.getValue().orElseThrow()))
            .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
            .toList();

    return averages.stream()
        .map(
            entry -> {
              var rank =
                  1
                      + (int)
                          averages.stream()
                              .filter(other -> other.getValue() > entry.getValue())
                              .count();
              return new RankingEntryDTO(entry.getKey(), entry.getValue(), rank);
            })
        .toList();
  }
}
