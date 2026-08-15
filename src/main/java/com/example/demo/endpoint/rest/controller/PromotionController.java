package com.example.demo.endpoint.rest.controller;

import com.example.demo.model.PromotionDTO;
import com.example.demo.service.PromotionService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class PromotionController {

  private final PromotionService promotionService;

  @GetMapping("/promotions")
  public ResponseEntity<List<PromotionDTO>> listPromotions() {
    return ResponseEntity.ok(promotionService.listPromotions());
  }
}
