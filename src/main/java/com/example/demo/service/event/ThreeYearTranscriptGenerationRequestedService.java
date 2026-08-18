package com.example.demo.service.event;

import com.example.demo.endpoint.event.model.ThreeYearTranscriptGenerationRequested;
import com.example.demo.exception.NotFoundException;
import com.example.demo.file.bucket.BucketComponent;
import com.example.demo.mail.Email;
import com.example.demo.mail.Mailer;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ThreeYearTranscriptGenerationService;
import com.example.demo.service.ThreeYearTranscriptPdfGenerator;
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
public class ThreeYearTranscriptGenerationRequestedService
    implements Consumer<ThreeYearTranscriptGenerationRequested> {

  private final ThreeYearTranscriptGenerationService threeYearTranscriptGenerationService;
  private final ThreeYearTranscriptPdfGenerator threeYearTranscriptPdfGenerator;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;
  private final UserRepository userRepository;

  @SneakyThrows
  @Override
  public void accept(ThreeYearTranscriptGenerationRequested event) {
    try {
      generateAndSend(event);
    } catch (Exception exception) {
      log.error(
          "Full transcript generation failed for student {}: {}",
          event.getStudentId(),
          exception.getMessage(),
          exception);
      throw exception;
    }
  }

  private void generateAndSend(ThreeYearTranscriptGenerationRequested event) throws Exception {
    var threeYearTranscript = threeYearTranscriptGenerationService.generate(event.getStudentId());
    var pdfFile = threeYearTranscriptPdfGenerator.generate(threeYearTranscript);
    var bucketKey = "transcripts/" + event.getStudentId() + "/full-path.pdf";
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
            "Your full academic transcript is ready",
            "Please find your complete 3-year transcript attached.",
            List.of(pdfFile));
    mailer.accept(email);
  }
}
