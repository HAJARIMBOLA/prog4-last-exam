package com.example.demo.model;

import jakarta.validation.constraints.Pattern;
import java.util.UUID;

public record GroupDTO(UUID id, @Pattern(regexp = "^K[1-9][0-9]*$") String ref) {}
