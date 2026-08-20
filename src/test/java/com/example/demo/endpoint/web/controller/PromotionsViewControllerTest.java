package com.example.demo.endpoint.web.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.model.PromotionDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtService;
import com.example.demo.security.SecurityConfig;
import com.example.demo.service.PromotionService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PromotionsViewController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  CustomUserDetailsService.class
})
class PromotionsViewControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private PromotionService promotionService;

  @MockBean private UserRepository userRepository;

  @Test
  @WithMockUser(roles = "ADMIN")
  void rendersPromotionsWithADownloadLinkPerPromotion() throws Exception {
    var promotionId = UUID.randomUUID();
    when(promotionService.listPromotions())
        .thenReturn(List.of(new PromotionDTO(promotionId, "P2026", 2029)));

    mockMvc
        .perform(get("/ui/promotions"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("P2026")))
        .andExpect(
            content()
                .string(
                    containsString("/promotions/" + promotionId + "/graduates/download?track=EL")))
        .andExpect(content().string(containsString("download-graduates-link")))
        .andExpect(content().string(containsString("/ui/promotions/" + promotionId + "/graduates")))
        .andExpect(content().string(containsString("view-graduates-link")));
  }
}
