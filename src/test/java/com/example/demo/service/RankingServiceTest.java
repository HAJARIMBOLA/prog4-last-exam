package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Promotion;
import com.example.demo.domain.Role;
import com.example.demo.domain.StudentEnrollment;
import com.example.demo.domain.Track;
import com.example.demo.domain.User;
import com.example.demo.model.RankingEntryDTO;
import com.example.demo.repository.StudentEnrollmentRepository;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

  @Mock private StudentEnrollmentRepository studentEnrollmentRepository;
  @Mock private ThreeYearAverageService threeYearAverageService;

  @Test
  void ranksSixStudentsByDescendingAverage() {
    var students = List.of(id(), id(), id(), id(), id(), id());
    var averages = List.of(18.0, 16.0, 14.0, 12.0, 11.0, 10.0);
    var studentAcademicYearIds = new LinkedHashMap<UUID, List<UUID>>();
    for (var i = 0; i < students.size(); i++) {
      var years = List.of(id());
      studentAcademicYearIds.put(students.get(i), years);
      when(threeYearAverageService.computeThreeYearAverage(students.get(i), years))
          .thenReturn(Optional.of(averages.get(i)));
    }

    var service = new RankingService(studentEnrollmentRepository, threeYearAverageService);
    var ranking = service.rankStudents(studentAcademicYearIds);

    assertThat(ranking).extracting(RankingEntryDTO::rank).containsExactly(1, 2, 3, 4, 5, 6);
    assertThat(ranking.get(0).studentId()).isEqualTo(students.get(0));
    assertThat(ranking.get(5).studentId()).isEqualTo(students.get(5));
  }

  @Test
  void tiedAveragesShareTheSameRankAndSkipTheNextOne() {
    var topA = id();
    var topB = id();
    var third = id();
    var studentAcademicYearIds = new LinkedHashMap<UUID, List<UUID>>();
    var yearsA = List.of(id());
    var yearsB = List.of(id());
    var yearsC = List.of(id());
    studentAcademicYearIds.put(topA, yearsA);
    studentAcademicYearIds.put(topB, yearsB);
    studentAcademicYearIds.put(third, yearsC);
    when(threeYearAverageService.computeThreeYearAverage(topA, yearsA))
        .thenReturn(Optional.of(15.0));
    when(threeYearAverageService.computeThreeYearAverage(topB, yearsB))
        .thenReturn(Optional.of(15.0));
    when(threeYearAverageService.computeThreeYearAverage(third, yearsC))
        .thenReturn(Optional.of(12.0));

    var service = new RankingService(studentEnrollmentRepository, threeYearAverageService);
    var ranking = service.rankStudents(studentAcademicYearIds);

    var rankByStudent =
        ranking.stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    RankingEntryDTO::studentId, RankingEntryDTO::rank));
    assertThat(rankByStudent.get(topA)).isEqualTo(1);
    assertThat(rankByStudent.get(topB)).isEqualTo(1);
    assertThat(rankByStudent.get(third)).isEqualTo(3);
  }

  @Test
  void rankingAPromotionByTrackOnlyIncludesStudentsOfThatTrack() {
    var promotionId = UUID.randomUUID();
    var elStudentId = UUID.randomUUID();
    var tnStudentId = UUID.randomUUID();
    var elYear = UUID.randomUUID();
    var tnYear = UUID.randomUUID();

    when(studentEnrollmentRepository.findByPromotionId(promotionId))
        .thenReturn(
            List.of(
                enrollmentOf(elStudentId, elYear, Track.EL, false),
                enrollmentOf(tnStudentId, tnYear, Track.TN, false)));
    when(threeYearAverageService.computeThreeYearAverage(elStudentId, List.of(elYear)))
        .thenReturn(Optional.of(14.0));

    var service = new RankingService(studentEnrollmentRepository, threeYearAverageService);
    var ranking = service.rankPromotionByTrack(promotionId, Track.EL);

    assertThat(ranking).hasSize(1);
    assertThat(ranking.get(0).studentId()).isEqualTo(elStudentId);
  }

  private StudentEnrollment enrollmentOf(
      UUID studentId, UUID academicYearId, Track track, boolean repeating) {
    var student =
        new User(studentId, "s@hei.school", "hash", Role.STUDENT, "STD001", "Jane", "Doe");
    var promotion = new Promotion(UUID.randomUUID(), "P2026", 2029);
    var academicYear = new AcademicYear(academicYearId, LocalDate.now(), LocalDate.now());
    return new StudentEnrollment(
        UUID.randomUUID(), student, promotion, academicYear, track, repeating);
  }

  private UUID id() {
    return UUID.randomUUID();
  }
}
