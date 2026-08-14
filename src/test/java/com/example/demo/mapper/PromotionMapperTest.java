package com.example.demo.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Promotion;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PromotionMapperTest {

  @Test
  void mapsEntityToDTO() {
    var promotion = new Promotion(UUID.randomUUID(), "Promotion 2026", 2026);
    var dto = PromotionMapper.toDTO(promotion);

    assertThat(dto.label()).isEqualTo("Promotion 2026");
    assertThat(dto.expectedGraduationYear()).isEqualTo(2026);
  }
}
