package com.example.demo.endpoint.web.controller;

import com.example.demo.domain.Track;
import com.example.demo.exception.NotFoundException;
import com.example.demo.mapper.PromotionMapper;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.service.GraduatesListService;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@AllArgsConstructor
public class GraduatesViewController {

  private final GraduatesListService graduatesListService;
  private final PromotionRepository promotionRepository;

  @GetMapping("/ui/promotions/{id}/graduates")
  @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
  public String viewGraduates(@PathVariable UUID id, Model model) {
    var promotion =
        promotionRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Promotion not found: " + id));

    model.addAttribute("promotion", PromotionMapper.toDTO(promotion));
    model.addAttribute("elGraduates", graduatesListService.listGraduates(id, Track.EL));
    model.addAttribute("tnGraduates", graduatesListService.listGraduates(id, Track.TN));
    return "graduates";
  }
}
