package com.example.demo.model;

import com.example.demo.domain.Role;
import java.util.UUID;

public record UserDTO(
    UUID id,
    String email,
    Role role,
    String matriculationNumber,
    String firstName,
    String lastName) {}
