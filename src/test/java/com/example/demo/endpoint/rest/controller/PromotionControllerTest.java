package com.example.demo.endpoint.rest.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.model.PromotionDTO;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtService;
import com.example.demo.service.PromotionService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PromotionController.class)
@AutoConfigureMockMvc(addFilters = false)
class PromotionControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PromotionService promotionService;

  @MockBean private JwtService jwtService;

  @MockBean private CustomUserDetailsService userDetailsService;

  @Test
  void listPromotionsReturnsPromotionDTOShape() throws Exception {
    var promotionId = UUID.randomUUID();
    when(promotionService.listPromotions())
        .thenReturn(List.of(new PromotionDTO(promotionId, "P2026", 2029)));

    mockMvc
        .perform(get("/promotions"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(promotionId.toString()))
        .andExpect(jsonPath("$[0].label").value("P2026"))
        .andExpect(jsonPath("$[0].expectedGraduationYear").value(2029));
  }
}
