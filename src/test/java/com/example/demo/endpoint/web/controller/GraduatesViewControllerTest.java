package com.example.demo.endpoint.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.domain.Promotion;
import com.example.demo.domain.Track;
import com.example.demo.model.GraduateEntryDTO;
import com.example.demo.repository.PromotionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtService;
import com.example.demo.security.SecurityConfig;
import com.example.demo.service.GraduatesListService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GraduatesViewController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  CustomUserDetailsService.class
})
class GraduatesViewControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private GraduatesListService graduatesListService;
  @MockBean private PromotionRepository promotionRepository;
  @MockBean private UserRepository userRepository;

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminSeesBothElAndTnGraduatesLists() throws Exception {
    var promotionId = UUID.randomUUID();
    var promotion = new Promotion(promotionId, "Promotion 2026", 2026);

    when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(promotion));
    when(graduatesListService.listGraduates(promotionId, Track.EL))
        .thenReturn(List.of(new GraduateEntryDTO(1, "STD001", "Jane", "Doe", 16.0)));
    when(graduatesListService.listGraduates(promotionId, Track.TN))
        .thenReturn(List.of(new GraduateEntryDTO(1, "STD002", "John", "Smith", 15.2)));

    mockMvc
        .perform(get("/ui/promotions/{id}/graduates", promotionId))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("STD001")))
        .andExpect(content().string(containsString("Jane")))
        .andExpect(content().string(containsString("STD002")))
        .andExpect(content().string(containsString("John")))
        .andExpect(content().string(containsString("Promotion 2026")));
  }

  @Test
  @WithMockUser(roles = "TEACHER")
  void teacherCanAlsoViewTheGraduatesList() throws Exception {
    var promotionId = UUID.randomUUID();
    var promotion = new Promotion(promotionId, "Promotion 2026", 2026);

    when(promotionRepository.findById(promotionId)).thenReturn(Optional.of(promotion));
    when(graduatesListService.listGraduates(promotionId, Track.EL)).thenReturn(List.of());
    when(graduatesListService.listGraduates(promotionId, Track.TN)).thenReturn(List.of());

    mockMvc.perform(get("/ui/promotions/{id}/graduates", promotionId)).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "STUDENT")
  void studentCannotViewTheGraduatesList() throws Exception {
    var promotionId = UUID.randomUUID();

    mockMvc
        .perform(get("/ui/promotions/{id}/graduates", promotionId))
        .andExpect(status().isForbidden());
  }
}
