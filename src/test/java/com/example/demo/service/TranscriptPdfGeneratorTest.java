package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.model.TranscriptDTO;
import com.example.demo.model.TranscriptLineDTO;
import com.lowagie.text.pdf.PdfReader;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TranscriptPdfGeneratorTest {

  private final TranscriptPdfGenerator generator = new TranscriptPdfGenerator();

  @Test
  void generatesANonEmptySinglePagePdf() throws Exception {
    var studentId = UUID.randomUUID();
    var academicYearId = UUID.randomUUID();
    var transcript =
        new TranscriptDTO(
            studentId,
            academicYearId,
            Instant.now(),
            List.of(
                new TranscriptLineDTO(
                    UUID.randomUUID(), "ALG101", "Algorithms", UUID.randomUUID(), 15.0)),
            false,
            15.0,
            60);

    var file = generator.generate(transcript);

    assertThat(file).exists();
    assertThat(file.length()).isPositive();

    var reader = new PdfReader(file.getAbsolutePath());
    try {
      assertThat(reader.getNumberOfPages()).isEqualTo(1);
    } finally {
      reader.close();
    }
  }

  @Test
  void includesEachTranscriptLineAsASection() throws Exception {
    var transcript =
        new TranscriptDTO(
            UUID.randomUUID(),
            UUID.randomUUID(),
            Instant.now(),
            List.of(
                new TranscriptLineDTO(
                    UUID.randomUUID(), "ALG101", "Algorithms", UUID.randomUUID(), 15.0),
                new TranscriptLineDTO(
                    UUID.randomUUID(), "DB101", "Databases", UUID.randomUUID(), null)),
            true,
            null,
            10);

    var file = generator.generate(transcript);

    var reader = new PdfReader(file.getAbsolutePath());
    String text;
    try {
      text = new com.lowagie.text.pdf.parser.PdfTextExtractor(reader).getTextFromPage(1);
    } finally {
      reader.close();
    }

    assertThat(text).contains("ALG101").contains("DB101").contains("PROVISIONAL");
  }
}
