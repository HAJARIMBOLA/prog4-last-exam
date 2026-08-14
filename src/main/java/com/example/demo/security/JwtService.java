package com.example.demo.security;

import com.example.demo.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

  private final SecretKey signingKey;
  private final long expirationMs;

  public JwtService(
      @Value("${jwt.secret}") String secret, @Value("${jwt.expiration-ms}") long expirationMs) {
    this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    this.expirationMs = expirationMs;
  }

  public String generateToken(User user) {
    var now = new Date();
    var expiry = new Date(now.getTime() + expirationMs);
    return Jwts.builder()
        .subject(user.getEmail())
        .claim("role", user.getRole().name())
        .issuedAt(now)
        .expiration(expiry)
        .signWith(signingKey)
        .compact();
  }

  public String extractEmail(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    var email = extractEmail(token);
    return email.equals(userDetails.getUsername()) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractClaim(token, Claims::getExpiration).before(new Date());
  }

  private <T> T extractClaim(String token, Function<Claims, T> resolver) {
    var claims = Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    return resolver.apply(claims);
  }
}
