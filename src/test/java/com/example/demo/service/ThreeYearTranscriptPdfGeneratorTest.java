package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.model.ThreeYearTranscriptDTO;
import com.example.demo.model.TranscriptDTO;
import com.example.demo.model.TranscriptLineDTO;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ThreeYearTranscriptPdfGeneratorTest {

  private final ThreeYearTranscriptPdfGenerator generator =
      new ThreeYearTranscriptPdfGenerator(new TranscriptPdfGenerator());

  @Test
  void generatesOnePagePerYearPlusAFinalSummaryPage() throws Exception {
    var studentId = UUID.randomUUID();
    var threeYearTranscript =
        new ThreeYearTranscriptDTO(
            studentId,
            List.of(
                transcript(studentId, "ALG101", 12.0),
                transcript(studentId, "DB101", 14.0),
                transcript(studentId, "SE101", 16.0)),
            14.0,
            Instant.now());

    var file = generator.generate(threeYearTranscript);

    assertThat(file).exists();
    assertThat(file.length()).isPositive();

    var reader = new PdfReader(file.getAbsolutePath());
    try {
      assertThat(reader.getNumberOfPages()).isEqualTo(4);
      var lastPageText = new PdfTextExtractor(reader).getTextFromPage(4);
      assertThat(lastPageText).contains("Final summary").contains("14.0");
    } finally {
      reader.close();
    }
  }

  private TranscriptDTO transcript(UUID studentId, String courseRef, double grade) {
    return new TranscriptDTO(
        studentId,
        UUID.randomUUID(),
        Instant.now(),
        List.of(
            new TranscriptLineDTO(
                UUID.randomUUID(), courseRef, courseRef, UUID.randomUUID(), grade)),
        false,
        grade,
        30);
  }
}
