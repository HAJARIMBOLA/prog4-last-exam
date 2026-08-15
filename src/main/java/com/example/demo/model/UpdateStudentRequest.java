package com.example.demo.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateStudentRequest(
    @NotBlank @Email String email,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank String matriculationNumber) {}
