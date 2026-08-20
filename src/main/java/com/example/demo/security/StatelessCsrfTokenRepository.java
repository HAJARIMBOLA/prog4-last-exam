package com.example.demo.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;

/**
 * Ignores token-clearing saves ({@code saveToken(null, ...)}). Spring Security's {@code
 * CsrfAuthenticationStrategy} issues one on every request here, since {@link
 * JwtAuthenticationFilter} re-authenticates from the JWT on every request (stateless, no session)
 * and each such authentication looks like a fresh login to {@code SessionManagementFilter}. Left
 * unhandled, that clear-then-regenerate pair races with the cookie a page just rendered, and
 * browsers unpredictably keep the delete over the valid token.
 */
public class StatelessCsrfTokenRepository implements CsrfTokenRepository {

  private final CsrfTokenRepository delegate;

  public StatelessCsrfTokenRepository(CsrfTokenRepository delegate) {
    this.delegate = delegate;
  }

  @Override
  public CsrfToken generateToken(HttpServletRequest request) {
    return delegate.generateToken(request);
  }

  @Override
  public void saveToken(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
    if (token == null) {
      return;
    }
    delegate.saveToken(token, request, response);
  }

  @Override
  public CsrfToken loadToken(HttpServletRequest request) {
    return delegate.loadToken(request);
  }
}
