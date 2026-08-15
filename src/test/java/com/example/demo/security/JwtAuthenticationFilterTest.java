package com.example.demo.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

  @Mock private JwtService jwtService;
  @Mock private CustomUserDetailsService userDetailsService;
  @Mock private FilterChain filterChain;

  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void requestWithoutAuthorizationHeaderContinuesTheChainWithoutAuthenticating() throws Exception {
    var filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    var request = new MockHttpServletRequest();
    var response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void headerWithoutBearerPrefixContinuesTheChainWithoutAuthenticating() throws Exception {
    var filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Basic dXNlcjpwYXNz");
    var response = new MockHttpServletResponse();

    filter.doFilterInternal(request, response, filterChain);

    verify(filterChain).doFilter(request, response);
    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
  }

  @Test
  void validTokenAuthenticatesTheRequest() throws Exception {
    var filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer valid-token");
    var response = new MockHttpServletResponse();
    var userDetails = userDetailsWith("t@hei.school");

    when(jwtService.extractEmail("valid-token")).thenReturn("t@hei.school");
    when(userDetailsService.loadUserByUsername("t@hei.school")).thenReturn(userDetails);
    when(jwtService.isTokenValid("valid-token", userDetails)).thenReturn(true);

    filter.doFilterInternal(request, response, filterChain);

    var authentication = SecurityContextHolder.getContext().getAuthentication();
    assertThat(authentication).isInstanceOf(UsernamePasswordAuthenticationToken.class);
    assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void invalidTokenDoesNotAuthenticateButStillContinuesTheChain() throws Exception {
    var filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer expired-token");
    var response = new MockHttpServletResponse();
    var userDetails = userDetailsWith("t@hei.school");

    when(jwtService.extractEmail("expired-token")).thenReturn("t@hei.school");
    when(userDetailsService.loadUserByUsername("t@hei.school")).thenReturn(userDetails);
    when(jwtService.isTokenValid("expired-token", userDetails)).thenReturn(false);

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void malformedTokenIsSwallowedAndTheChainStillContinues() throws Exception {
    var filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer garbage");
    var response = new MockHttpServletResponse();

    when(jwtService.extractEmail("garbage")).thenThrow(new JwtException("malformed"));

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void unknownUserIsSwallowedAndTheChainStillContinues() throws Exception {
    var filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer valid-token");
    var response = new MockHttpServletResponse();

    when(jwtService.extractEmail("valid-token")).thenReturn("ghost@hei.school");
    when(userDetailsService.loadUserByUsername("ghost@hei.school"))
        .thenThrow(new UsernameNotFoundException("User not found: ghost@hei.school"));

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    verify(filterChain).doFilter(request, response);
  }

  @Test
  void anAlreadyAuthenticatedContextIsNeverOverwritten() throws Exception {
    var filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
    var request = new MockHttpServletRequest();
    request.addHeader("Authorization", "Bearer valid-token");
    var response = new MockHttpServletResponse();
    var existingAuthentication =
        new UsernamePasswordAuthenticationToken("already", null, List.of());
    SecurityContextHolder.getContext().setAuthentication(existingAuthentication);

    when(jwtService.extractEmail("valid-token")).thenReturn("t@hei.school");

    filter.doFilterInternal(request, response, filterChain);

    assertThat(SecurityContextHolder.getContext().getAuthentication())
        .isSameAs(existingAuthentication);
    verify(userDetailsService, never()).loadUserByUsername(org.mockito.ArgumentMatchers.any());
    verify(filterChain).doFilter(request, response);
  }

  private UserDetails userDetailsWith(String email) {
    return org.springframework.security.core.userdetails.User.builder()
        .username(email)
        .password("hash")
        .authorities(new SimpleGrantedAuthority("ROLE_TEACHER"))
        .build();
  }
}
