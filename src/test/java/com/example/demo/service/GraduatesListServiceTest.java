package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Role;
import com.example.demo.domain.Track;
import com.example.demo.domain.User;
import com.example.demo.model.RankingEntryDTO;
import com.example.demo.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraduatesListServiceTest {

  @Mock private RankingService rankingService;
  @Mock private GraduationService graduationService;
  @Mock private UserRepository userRepository;

  @Test
  void returnsOnlyEligibleGraduatesSortedByRankWithStudentInfo() {
    var promotionId = UUID.randomUUID();
    var rank1Student = UUID.randomUUID();
    var rank2Student = UUID.randomUUID();
    var notGraduatingStudent = UUID.randomUUID();

    when(rankingService.rankPromotionByTrack(promotionId, Track.EL))
        .thenReturn(
            List.of(
                new RankingEntryDTO(rank2Student, 14.0, 2),
                new RankingEntryDTO(rank1Student, 16.0, 1),
                new RankingEntryDTO(notGraduatingStudent, 8.0, 3)));
    when(graduationService.isEligibleForGraduation(rank1Student)).thenReturn(true);
    when(graduationService.isEligibleForGraduation(rank2Student)).thenReturn(true);
    when(graduationService.isEligibleForGraduation(notGraduatingStudent)).thenReturn(false);
    when(userRepository.findById(rank1Student))
        .thenReturn(Optional.of(userWith(rank1Student, "STD001", "Doe", "Jane")));
    when(userRepository.findById(rank2Student))
        .thenReturn(Optional.of(userWith(rank2Student, "STD002", "Smith", "John")));

    var service = new GraduatesListService(rankingService, graduationService, userRepository);
    var graduates = service.listGraduates(promotionId, Track.EL);

    assertThat(graduates).hasSize(2);
    assertThat(graduates.get(0).rank()).isEqualTo(1);
    assertThat(graduates.get(0).matriculationNumber()).isEqualTo("STD001");
    assertThat(graduates.get(0).firstName()).isEqualTo("Jane");
    assertThat(graduates.get(0).lastName()).isEqualTo("Doe");
    assertThat(graduates.get(0).average()).isEqualTo(16.0);
    assertThat(graduates.get(1).rank()).isEqualTo(2);
    assertThat(graduates.get(1).matriculationNumber()).isEqualTo("STD002");
  }

  @Test
  void returnsAnEmptyListWhenNoStudentIsEligible() {
    var promotionId = UUID.randomUUID();
    var notGraduatingStudent = UUID.randomUUID();

    when(rankingService.rankPromotionByTrack(promotionId, Track.TN))
        .thenReturn(List.of(new RankingEntryDTO(notGraduatingStudent, 9.5, 1)));
    when(graduationService.isEligibleForGraduation(notGraduatingStudent)).thenReturn(false);

    var service = new GraduatesListService(rankingService, graduationService, userRepository);
    var graduates = service.listGraduates(promotionId, Track.TN);

    assertThat(graduates).isEmpty();
  }

  private User userWith(UUID id, String matriculationNumber, String lastName, String firstName) {
    return new User(
        id, "s@hei.school", "hash", Role.STUDENT, matriculationNumber, firstName, lastName);
  }
}
