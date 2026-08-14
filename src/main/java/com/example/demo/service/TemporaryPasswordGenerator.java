package com.example.demo.service;

import java.security.SecureRandom;

public class TemporaryPasswordGenerator {

  private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
  private static final int LENGTH = 12;
  private static final SecureRandom RANDOM = new SecureRandom();

  private TemporaryPasswordGenerator() {}

  public static String generate() {
    var builder = new StringBuilder(LENGTH);
    for (int i = 0; i < LENGTH; i++) {
      builder.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
    }
    return builder.toString();
  }
}
