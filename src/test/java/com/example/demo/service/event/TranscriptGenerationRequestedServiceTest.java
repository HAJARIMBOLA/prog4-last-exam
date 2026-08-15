package com.example.demo.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.endpoint.event.model.TranscriptGenerationRequested;
import com.example.demo.file.bucket.BucketComponent;
import com.example.demo.mail.Email;
import com.example.demo.mail.Mailer;
import com.example.demo.model.TranscriptDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.TranscriptGenerationService;
import com.example.demo.service.TranscriptPdfGenerator;
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
class TranscriptGenerationRequestedServiceTest {

  @Mock private TranscriptGenerationService transcriptGenerationService;
  @Mock private TranscriptPdfGenerator transcriptPdfGenerator;
  @Mock private BucketComponent bucketComponent;
  @Mock private Mailer mailer;
  @Mock private UserRepository userRepository;

  @Test
  void uploadsThePdfAndEmailsItToTheStudent() throws Exception {
    var studentId = UUID.randomUUID();
    var academicYearId = UUID.randomUUID();
    var transcript =
        new TranscriptDTO(studentId, academicYearId, Instant.now(), List.of(), false, 14.0, 60);
    var pdfFile = File.createTempFile("transcript-test", ".pdf");
    var student =
        new User(studentId, "s@hei.school", "hash", Role.STUDENT, "STD001", "Jane", "Doe");

    when(transcriptGenerationService.generate(studentId, academicYearId)).thenReturn(transcript);
    when(transcriptPdfGenerator.generate(transcript)).thenReturn(pdfFile);
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));

    var service =
        new TranscriptGenerationRequestedService(
            transcriptGenerationService,
            transcriptPdfGenerator,
            bucketComponent,
            mailer,
            userRepository);

    var event =
        TranscriptGenerationRequested.builder()
            .studentId(studentId)
            .academicYearId(academicYearId)
            .build();
    service.accept(event);

    verify(bucketComponent)
        .upload(eq(pdfFile), eq("transcripts/" + studentId + "/" + academicYearId + ".pdf"));

    var emailCaptor = ArgumentCaptor.forClass(Email.class);
    verify(mailer).accept(emailCaptor.capture());
    var sentEmail = emailCaptor.getValue();
    assertThat(sentEmail.to().getAddress()).isEqualTo("s@hei.school");
    assertThat(sentEmail.attachments()).containsExactly(pdfFile);
  }
}
