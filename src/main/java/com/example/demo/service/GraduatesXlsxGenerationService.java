package com.example.demo.service;

import com.example.demo.domain.Track;
import com.example.demo.repository.UserRepository;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GraduatesXlsxGenerationService {

  private static final List<String> HEADERS =
      List.of("rank", "STD", "last name", "first name", "general average");

  private final RankingService rankingService;
  private final GraduationService graduationService;
  private final UserRepository userRepository;

  @SneakyThrows
  public File generate(UUID promotionId, Track track) {
    var graduates =
        rankingService.rankPromotionByTrack(promotionId, track).stream()
            .filter(entry -> graduationService.isEligibleForGraduation(entry.studentId()))
            .sorted(Comparator.comparingInt(entry -> entry.rank()))
            .toList();

    try (var workbook = new XSSFWorkbook()) {
      var sheet = workbook.createSheet("Graduates");
      var headerRow = sheet.createRow(0);
      for (var i = 0; i < HEADERS.size(); i++) {
        headerRow.createCell(i).setCellValue(HEADERS.get(i));
      }

      var rowIndex = 1;
      for (var entry : graduates) {
        var student =
            userRepository
                .findById(entry.studentId())
                .orElseThrow(
                    () -> new IllegalArgumentException("Student not found: " + entry.studentId()));
        var row = sheet.createRow(rowIndex++);
        row.createCell(0).setCellValue(entry.rank());
        row.createCell(1).setCellValue(student.getMatriculationNumber());
        row.createCell(2).setCellValue(student.getLastName());
        row.createCell(3).setCellValue(student.getFirstName());
        row.createCell(4).setCellValue(entry.average());
      }

      var file = File.createTempFile("graduates-" + promotionId + "-" + track, ".xlsx");
      try (var outputStream = new FileOutputStream(file)) {
        workbook.write(outputStream);
      }
      return file;
    }
  }
}
