package com.example.demo.service;

import com.example.demo.domain.Track;
import com.example.demo.file.bucket.BucketComponent;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class GraduatesXlsxPublishingService {

  private static final Duration DOWNLOAD_LINK_DURATION = Duration.ofMinutes(15);

  private final GraduatesXlsxGenerationService graduatesXlsxGenerationService;
  private final BucketComponent bucketComponent;

  public URL publish(UUID promotionId, Track track) {
    var file = graduatesXlsxGenerationService.generate(promotionId, track);
    var bucketKey = "graduates/" + promotionId + "/" + track + ".xlsx";
    bucketComponent.upload(file, bucketKey);
    return bucketComponent.presign(bucketKey, DOWNLOAD_LINK_DURATION);
  }
}
