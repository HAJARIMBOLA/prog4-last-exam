package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ThreeYearAverageServiceTest {

  @Mock private YearAverageService yearAverageService;

  @Test
  void averagesTheThreeYearlyAverages() {
    var studentId = UUID.randomUUID();
    var year1 = UUID.randomUUID();
    var year2 = UUID.randomUUID();
    var year3 = UUID.randomUUID();
    when(yearAverageService.computeYearAverage(studentId, year1)).thenReturn(Optional.of(12.0));
    when(yearAverageService.computeYearAverage(studentId, year2)).thenReturn(Optional.of(14.0));
    when(yearAverageService.computeYearAverage(studentId, year3)).thenReturn(Optional.of(16.0));

    var service = new ThreeYearAverageService(yearAverageService);

    assertThat(service.computeThreeYearAverage(studentId, List.of(year1, year2, year3)))
        .contains(14.0);
  }

  @Test
  void noYearHasAnAverageReturnsEmpty() {
    var studentId = UUID.randomUUID();
    var year1 = UUID.randomUUID();
    when(yearAverageService.computeYearAverage(studentId, year1)).thenReturn(Optional.empty());

    var service = new ThreeYearAverageService(yearAverageService);

    assertThat(service.computeThreeYearAverage(studentId, List.of(year1))).isEmpty();
  }
}
