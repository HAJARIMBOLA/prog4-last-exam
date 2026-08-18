package com.example.demo.service;

import com.example.demo.model.ThreeYearTranscriptDTO;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ThreeYearTranscriptPdfGenerator {

  private final TranscriptPdfGenerator transcriptPdfGenerator;

  @SneakyThrows
  public File generate(ThreeYearTranscriptDTO threeYearTranscript) {
    var file = File.createTempFile("full-transcript-" + threeYearTranscript.studentId(), ".pdf");

    try (var outputStream = new FileOutputStream(file)) {
      var document = new Document();
      PdfWriter.getInstance(document, outputStream);
      document.open();

      var yearlyTranscripts = threeYearTranscript.yearlyTranscripts();
      for (var i = 0; i < yearlyTranscripts.size(); i++) {
        if (i > 0) {
          document.newPage();
        }
        transcriptPdfGenerator.writeYearSection(
            document, yearlyTranscripts.get(i), "Year " + (i + 1));
      }

      document.newPage();
      document.add(new Paragraph("Final summary"));
      document.add(
          new Paragraph(
              "Final average: "
                  + (threeYearTranscript.finalAverage() == null
                      ? "N/A"
                      : threeYearTranscript.finalAverage())));

      document.close();
    }

    return file;
  }
}
