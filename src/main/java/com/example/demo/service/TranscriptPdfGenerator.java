package com.example.demo.service;

import com.example.demo.model.TranscriptDTO;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
public class TranscriptPdfGenerator {

  @SneakyThrows
  public File generate(TranscriptDTO transcript) {
    var file =
        File.createTempFile(
            "transcript-" + transcript.studentId() + "-" + transcript.academicYearId(), ".pdf");

    try (var outputStream = new FileOutputStream(file)) {
      var document = new Document();
      PdfWriter.getInstance(document, outputStream);
      document.open();
      writeYearSection(document, transcript, "Transcript");
      document.close();
    }

    return file;
  }

  @SneakyThrows
  void writeYearSection(Document document, TranscriptDTO transcript, String heading) {
    document.add(new Paragraph(heading));
    document.add(
        new Paragraph("Status: " + (transcript.provisional() ? "PROVISIONAL" : "COMPLETE")));
    document.add(
        new Paragraph(
            "General average: "
                + (transcript.generalAverage() == null ? "N/A" : transcript.generalAverage())));
    document.add(new Paragraph("Total credits: " + transcript.totalCredits()));
    document.add(new Paragraph(" "));

    for (var line : transcript.lines()) {
      document.add(
          new Paragraph(
              line.courseRef()
                  + " - "
                  + line.courseTitle()
                  + ": "
                  + (line.grade() == null ? "N/A" : line.grade())));
    }
  }
}
