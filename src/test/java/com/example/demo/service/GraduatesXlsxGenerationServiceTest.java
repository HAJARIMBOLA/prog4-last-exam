package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Role;
import com.example.demo.domain.Track;
import com.example.demo.domain.User;
import com.example.demo.model.RankingEntryDTO;
import com.example.demo.repository.UserRepository;
import java.io.FileInputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraduatesXlsxGenerationServiceTest {

  @Mock private RankingService rankingService;
  @Mock private GraduationService graduationService;
  @Mock private UserRepository userRepository;

  @Test
  void writesHeaderAndRowsSortedByRankOnlyForEligibleGraduates() throws Exception {
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

    var service =
        new GraduatesXlsxGenerationService(rankingService, graduationService, userRepository);
    var file = service.generate(promotionId, Track.EL);

    try (var inputStream = new FileInputStream(file);
        var workbook = new XSSFWorkbook(inputStream)) {
      var sheet = workbook.getSheetAt(0);
      var header = sheet.getRow(0);
      assertThat(header.getCell(0).getStringCellValue()).isEqualTo("rank");
      assertThat(header.getCell(1).getStringCellValue()).isEqualTo("STD");
      assertThat(header.getCell(2).getStringCellValue()).isEqualTo("last name");
      assertThat(header.getCell(3).getStringCellValue()).isEqualTo("first name");
      assertThat(header.getCell(4).getStringCellValue()).isEqualTo("general average");

      var firstDataRow = sheet.getRow(1);
      assertThat(firstDataRow.getCell(0).getNumericCellValue()).isEqualTo(1.0);
      assertThat(firstDataRow.getCell(1).getStringCellValue()).isEqualTo("STD001");

      var secondDataRow = sheet.getRow(2);
      assertThat(secondDataRow.getCell(0).getNumericCellValue()).isEqualTo(2.0);
      assertThat(secondDataRow.getCell(1).getStringCellValue()).isEqualTo("STD002");

      assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(3);
    }
  }

  private User userWith(UUID id, String matriculationNumber, String lastName, String firstName) {
    return new User(
        id, "s@hei.school", "hash", Role.STUDENT, matriculationNumber, firstName, lastName);
  }
}
