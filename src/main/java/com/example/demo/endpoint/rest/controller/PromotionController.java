package com.example.demo.endpoint.rest.controller;

import com.example.demo.model.PageResponseDTO;
import com.example.demo.model.PromotionDTO;
import com.example.demo.service.PromotionService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class PromotionController {

  private final PromotionService promotionService;

  @GetMapping("/promotions")
  public ResponseEntity<PageResponseDTO<PromotionDTO>> listPromotions(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
    return ResponseEntity.ok(promotionService.listPromotions(PageRequest.of(page, size)));
  }
}
