package com.example.demo.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String BEARER_PREFIX = "Bearer ";
  private static final String AUTH_TOKEN_COOKIE = "auth_token";

  private final JwtService jwtService;
  private final CustomUserDetailsService userDetailsService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    var authHeader = request.getHeader(AUTHORIZATION_HEADER);
    String token;
    if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
      token = authHeader.substring(BEARER_PREFIX.length());
    } else {
      var cookieToken = extractTokenFromCookie(request);
      if (cookieToken.isEmpty()) {
        filterChain.doFilter(request, response);
        return;
      }
      token = cookieToken.get();
    }

    try {
      var email = jwtService.extractEmail(token);
      if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        var userDetails = userDetailsService.loadUserByUsername(email);
        if (jwtService.isTokenValid(token, userDetails)) {
          var authToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }
    } catch (JwtException | UsernameNotFoundException ignored) {
    }

    filterChain.doFilter(request, response);
  }

  private Optional<String> extractTokenFromCookie(HttpServletRequest request) {
    var cookies = request.getCookies();
    if (cookies == null) {
      return Optional.empty();
    }
    return Arrays.stream(cookies)
        .filter(cookie -> cookie.getName().equals(AUTH_TOKEN_COOKIE))
        .map(Cookie::getValue)
        .findFirst();
  }
}
