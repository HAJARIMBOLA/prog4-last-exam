package com.example.demo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record CourseDTO(
    UUID id, @NotBlank String ref, @NotBlank String title, @Positive int credits) {}
