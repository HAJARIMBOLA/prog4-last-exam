package com.example.demo.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.endpoint.event.model.ThreeYearTranscriptGenerationRequested;
import com.example.demo.file.bucket.BucketComponent;
import com.example.demo.mail.Email;
import com.example.demo.mail.Mailer;
import com.example.demo.model.ThreeYearTranscriptDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.ThreeYearTranscriptGenerationService;
import com.example.demo.service.ThreeYearTranscriptPdfGenerator;
import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ThreeYearTranscriptGenerationRequestedServiceTest {

  @Mock private ThreeYearTranscriptGenerationService threeYearTranscriptGenerationService;
  @Mock private ThreeYearTranscriptPdfGenerator threeYearTranscriptPdfGenerator;
  @Mock private BucketComponent bucketComponent;
  @Mock private Mailer mailer;
  @Mock private UserRepository userRepository;

  @Test
  void uploadsThePdfAndEmailsItToTheStudent() throws Exception {
    var studentId = UUID.randomUUID();
    var threeYearTranscript = new ThreeYearTranscriptDTO(studentId, List.of(), 14.0, Instant.now());
    var pdfFile = File.createTempFile("full-transcript-test", ".pdf");
    var student =
        new User(studentId, "s@hei.school", "hash", Role.STUDENT, "STD001", "Jane", "Doe");

    when(threeYearTranscriptGenerationService.generate(studentId)).thenReturn(threeYearTranscript);
    when(threeYearTranscriptPdfGenerator.generate(threeYearTranscript)).thenReturn(pdfFile);
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

    var service =
        new ThreeYearTranscriptGenerationRequestedService(
            threeYearTranscriptGenerationService,
            threeYearTranscriptPdfGenerator,
            bucketComponent,
            mailer,
            userRepository);

    var event = ThreeYearTranscriptGenerationRequested.builder().studentId(studentId).build();
    service.accept(event);

    verify(bucketComponent).upload(eq(pdfFile), eq("transcripts/" + studentId + "/full-path.pdf"));

    var emailCaptor = ArgumentCaptor.forClass(Email.class);
    verify(mailer).accept(emailCaptor.capture());
    var sentEmail = emailCaptor.getValue();
    assertThat(sentEmail.to().getAddress()).isEqualTo("s@hei.school");
    assertThat(sentEmail.attachments()).containsExactly(pdfFile);
  }

  @Test
  void aPdfGenerationFailurePropagatesToTriggerThePojaRetryMechanism() {
    var studentId = UUID.randomUUID();
    var threeYearTranscript = new ThreeYearTranscriptDTO(studentId, List.of(), 14.0, Instant.now());

    when(threeYearTranscriptGenerationService.generate(studentId)).thenReturn(threeYearTranscript);
    when(threeYearTranscriptPdfGenerator.generate(threeYearTranscript))
        .thenThrow(new RuntimeException("PDF rendering failed"));

    var service =
        new ThreeYearTranscriptGenerationRequestedService(
            threeYearTranscriptGenerationService,
            threeYearTranscriptPdfGenerator,
            bucketComponent,
            mailer,
            userRepository);

    var event = ThreeYearTranscriptGenerationRequested.builder().studentId(studentId).build();

    assertThatThrownBy(() -> service.accept(event))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("PDF rendering failed");

    verify(bucketComponent, never())
        .upload(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    verify(mailer, never()).accept(org.mockito.ArgumentMatchers.any());
  }
}
