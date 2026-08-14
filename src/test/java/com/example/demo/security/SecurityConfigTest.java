package com.example.demo.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.endpoint.rest.controller.health.PingController;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = PingController.class)
@Import({
  SecurityConfig.class,
  JwtAuthenticationFilter.class,
  JwtService.class,
  CustomUserDetailsService.class
})
class SecurityConfigTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private UserRepository userRepository;

  @Test
  void securityContextLoads() {
    assertThat(mockMvc).isNotNull();
  }

  @Test
  void publicPingEndpointIsAccessibleWithoutToken() throws Exception {
    mockMvc.perform(get("/ping")).andExpect(status().isOk());
  }

  @Test
  void protectedEndpointReturnsUnauthorizedWithoutToken() throws Exception {
    mockMvc.perform(get("/some-protected-resource")).andExpect(status().isUnauthorized());
  }
}
