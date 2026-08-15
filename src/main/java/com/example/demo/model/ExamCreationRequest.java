package com.example.demo.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ExamCreationRequest(
    @NotNull LocalDate dateExam,
    @DecimalMin(value = "0.0", inclusive = false) @DecimalMax(value = "1.0") double coefficient) {}
