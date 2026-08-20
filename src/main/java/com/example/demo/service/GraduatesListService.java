package com.example.demo.service;

import com.example.demo.domain.Track;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.GraduateEntryDTO;
import com.example.demo.model.RankingEntryDTO;
import com.example.demo.repository.UserRepository;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GraduatesListService {

  private final RankingService rankingService;
  private final GraduationService graduationService;
  private final UserRepository userRepository;

  public List<GraduateEntryDTO> listGraduates(UUID promotionId, Track track) {
    return rankingService.rankPromotionByTrack(promotionId, track).stream()
        .filter(entry -> graduationService.isEligibleForGraduation(entry.studentId()))
        .sorted(Comparator.comparingInt(RankingEntryDTO::rank))
        .map(this::toGraduateEntry)
        .toList();
  }

  private GraduateEntryDTO toGraduateEntry(RankingEntryDTO entry) {
    var student =
        userRepository
            .findById(entry.studentId())
            .orElseThrow(() -> new NotFoundException("Student not found: " + entry.studentId()));
    return new GraduateEntryDTO(
        entry.rank(),
        student.getMatriculationNumber(),
        student.getFirstName(),
        student.getLastName(),
        entry.average());
  }
}
