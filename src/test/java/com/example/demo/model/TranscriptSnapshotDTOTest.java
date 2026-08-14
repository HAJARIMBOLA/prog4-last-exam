package com.example.demo.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TranscriptSnapshotDTOTest {

  @Test
  void snapshotIsNotAffectedByMutatingTheSourceListAfterConstruction() {
    var mutableLines = new ArrayList<TranscriptLineDTO>();
    mutableLines.add(
        new TranscriptLineDTO(UUID.randomUUID(), "ALG101", "Algorithms", UUID.randomUUID(), 15.0));

    var snapshot =
        new TranscriptSnapshotDTO(
            UUID.randomUUID(), UUID.randomUUID(), Instant.now(), mutableLines);

    mutableLines.add(
        new TranscriptLineDTO(UUID.randomUUID(), "EXTRA", "Extra Course", UUID.randomUUID(), 10.0));

    assertThat(snapshot.lines()).hasSize(1);
  }

  @Test
  void snapshotLinesCannotBeMutatedDirectly() {
    var snapshot =
        new TranscriptSnapshotDTO(
            UUID.randomUUID(),
            UUID.randomUUID(),
            Instant.now(),
            List.of(
                new TranscriptLineDTO(
                    UUID.randomUUID(), "ALG101", "Algorithms", UUID.randomUUID(), 15.0)));

    assertThatThrownBy(
            () ->
                snapshot
                    .lines()
                    .add(
                        new TranscriptLineDTO(
                            UUID.randomUUID(), "EXTRA", "Extra Course", UUID.randomUUID(), 10.0)))
        .isInstanceOf(UnsupportedOperationException.class);
  }
}
