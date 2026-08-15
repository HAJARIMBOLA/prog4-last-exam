package com.example.demo.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateTeacherRequest(
    @NotBlank @Email String email, @NotBlank String firstName, @NotBlank String lastName) {}
