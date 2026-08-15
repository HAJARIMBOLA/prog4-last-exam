package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Promotion;
import com.example.demo.repository.PromotionRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

  @Mock private PromotionRepository promotionRepository;

  @Test
  void listPromotionsMapsEntitiesToDTOs() {
    var promotion = new Promotion(UUID.randomUUID(), "P2026", 2029);
    when(promotionRepository.findAll()).thenReturn(List.of(promotion));

    var service = new PromotionService(promotionRepository);
    var result = service.listPromotions();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).label()).isEqualTo("P2026");
    assertThat(result.get(0).expectedGraduationYear()).isEqualTo(2029);
  }
}
