package com.example.demo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record PromotionDTO(
    UUID id, @NotBlank String label, @NotNull @Positive Integer expectedGraduationYear) {}
