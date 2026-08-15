package com.example.demo.endpoint.rest.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.domain.Track;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtAuthenticationFilter;
import com.example.demo.security.JwtService;
import com.example.demo.security.SecurityConfig;
import com.example.demo.service.GraduatesXlsxPublishingService;
import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GraduatesDownloadController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  CustomUserDetailsService.class
})
class GraduatesDownloadControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private GraduatesXlsxPublishingService graduatesXlsxPublishingService;

  @MockBean private UserRepository userRepository;

  @Test
  @WithMockUser(roles = "ADMIN")
  void adminCanDownloadTheGraduatesList() throws Exception {
    var promotionId = UUID.randomUUID();
    var url = URI.create("https://bucket.example.com/graduates.xlsx").toURL();
    when(graduatesXlsxPublishingService.publish(promotionId, Track.EL)).thenReturn(url);

    mockMvc
        .perform(get("/promotions/{id}/graduates/download", promotionId).param("track", "EL"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.downloadUrl").value(url.toString()));
  }

  @Test
  @WithMockUser(roles = "TEACHER")
  void teacherCanDownloadTheGraduatesList() throws Exception {
    var promotionId = UUID.randomUUID();
    var url = URI.create("https://bucket.example.com/graduates.xlsx").toURL();
    when(graduatesXlsxPublishingService.publish(promotionId, Track.TN)).thenReturn(url);

    mockMvc
        .perform(get("/promotions/{id}/graduates/download", promotionId).param("track", "TN"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(roles = "STUDENT")
  void studentCannotDownloadTheGraduatesList() throws Exception {
    mockMvc
        .perform(get("/promotions/{id}/graduates/download", UUID.randomUUID()).param("track", "EL"))
        .andExpect(status().isForbidden());
  }
}
