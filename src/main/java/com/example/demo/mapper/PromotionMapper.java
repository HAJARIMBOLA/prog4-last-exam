package com.example.demo.mapper;

import com.example.demo.domain.Promotion;
import com.example.demo.model.PromotionDTO;

public class PromotionMapper {

  private PromotionMapper() {}

  public static PromotionDTO toDTO(Promotion promotion) {
    return new PromotionDTO(
        promotion.getId(), promotion.getLabel(), promotion.getExpectedGraduationYear());
  }
}
