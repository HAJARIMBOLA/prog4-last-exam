package com.example.demo.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ThreeYearAverageService {

  private final YearAverageService yearAverageService;

  public Optional<Double> computeThreeYearAverage(UUID studentId, List<UUID> academicYearIds) {
    var yearAverages =
        academicYearIds.stream()
            .map(academicYearId -> yearAverageService.computeYearAverage(studentId, academicYearId))
            .flatMap(Optional::stream)
            .toList();

    if (yearAverages.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(
        yearAverages.stream().mapToDouble(Double::doubleValue).average().orElseThrow());
  }
}
