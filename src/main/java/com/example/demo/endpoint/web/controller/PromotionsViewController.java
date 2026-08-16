package com.example.demo.endpoint.web.controller;

import com.example.demo.service.PromotionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AllArgsConstructor
public class PromotionsViewController {

  private final PromotionService promotionService;

  @GetMapping("/ui/promotions")
  public String listPromotions(Model model) {
    model.addAttribute("promotions", promotionService.listPromotions());
    return "promotions";
  }
}
