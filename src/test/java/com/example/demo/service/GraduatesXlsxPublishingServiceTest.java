package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Track;
import com.example.demo.file.bucket.BucketComponent;
import java.io.File;
import java.net.URI;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraduatesXlsxPublishingServiceTest {

  @Mock private GraduatesXlsxGenerationService graduatesXlsxGenerationService;
  @Mock private BucketComponent bucketComponent;

  @Test
  void uploadsTheGeneratedFileAndReturnsAPresignedUrl() throws Exception {
    var promotionId = UUID.randomUUID();
    var file = File.createTempFile("graduates-test", ".xlsx");
    var expectedUrl = URI.create("https://bucket.example.com/graduates.xlsx").toURL();
    var expectedKey = "graduates/" + promotionId + "/" + Track.EL + ".xlsx";

    when(graduatesXlsxGenerationService.generate(promotionId, Track.EL)).thenReturn(file);
    when(bucketComponent.presign(expectedKey, Duration.ofMinutes(15))).thenReturn(expectedUrl);

    var service =
        new GraduatesXlsxPublishingService(graduatesXlsxGenerationService, bucketComponent);
    var url = service.publish(promotionId, Track.EL);

    verify(bucketComponent).upload(file, expectedKey);
    assertThat(url).isEqualTo(expectedUrl);
  }
}
