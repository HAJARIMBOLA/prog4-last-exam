package com.example.demo.service.event;

import com.example.demo.endpoint.event.model.TranscriptGenerationRequested;
import com.example.demo.exception.NotFoundException;
import com.example.demo.file.bucket.BucketComponent;
import com.example.demo.mail.Email;
import com.example.demo.mail.Mailer;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.TranscriptGenerationService;
import com.example.demo.service.TranscriptPdfGenerator;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class TranscriptGenerationRequestedService
    implements Consumer<TranscriptGenerationRequested> {

  private final TranscriptGenerationService transcriptGenerationService;
  private final TranscriptPdfGenerator transcriptPdfGenerator;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;
  private final UserRepository userRepository;

  @SneakyThrows
  @Override
  public void accept(TranscriptGenerationRequested event) {
    try {
      generateAndSend(event);
    } catch (Exception exception) {
      log.error(
          "Transcript generation failed for student {} academic year {}: {}",
          event.getStudentId(),
          event.getAcademicYearId(),
          exception.getMessage(),
          exception);
      throw exception;
    }
  }

  private void generateAndSend(TranscriptGenerationRequested event) throws Exception {
    var transcript =
        transcriptGenerationService.generate(event.getStudentId(), event.getAcademicYearId());
    var pdfFile = transcriptPdfGenerator.generate(transcript);
    var bucketKey =
        "transcripts/" + event.getStudentId() + "/" + event.getAcademicYearId() + ".pdf";
    bucketComponent.upload(pdfFile, bucketKey);

    var student =
        userRepository
            .findById(event.getStudentId())
            .orElseThrow(() -> new NotFoundException("Student not found: " + event.getStudentId()));

    var email =
        new Email(
            new InternetAddress(student.getEmail()),
            List.of(),
            List.of(),
            "Your transcript is ready",
            "Please find your transcript attached.",
            List.of(pdfFile));
    mailer.accept(email);
  }
}
