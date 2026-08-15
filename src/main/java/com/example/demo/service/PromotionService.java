package com.example.demo.service;

import com.example.demo.mapper.PromotionMapper;
import com.example.demo.model.PromotionDTO;
import com.example.demo.repository.PromotionRepository;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PromotionService {

  private final PromotionRepository promotionRepository;

  public List<PromotionDTO> listPromotions() {
    return promotionRepository.findAll().stream().map(PromotionMapper::toDTO).toList();
  }
}
