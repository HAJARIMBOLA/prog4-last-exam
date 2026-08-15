package com.example.demo.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record GradeSubmissionRequest(
    @NotNull UUID studentId, @NotNull @DecimalMin("0.0") @DecimalMax("20.0") Double value) {}
