package com.example.demo.service;

import com.example.demo.repository.UserRepository;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MatriculationNumberValidator {

  private static final Pattern FORMAT = Pattern.compile("^STD\\d+$");

  private final UserRepository userRepository;

  public void validate(String matriculationNumber) {
    if (matriculationNumber == null || !FORMAT.matcher(matriculationNumber).matches()) {
      throw new IllegalArgumentException(
          "Matriculation number must match the STD<digits> format: " + matriculationNumber);
    }
    if (userRepository.existsByMatriculationNumber(matriculationNumber)) {
      throw new IllegalArgumentException(
          "Matriculation number already in use: " + matriculationNumber);
    }
  }
}
