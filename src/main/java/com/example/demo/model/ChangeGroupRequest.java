package com.example.demo.model;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record ChangeGroupRequest(@NotNull UUID groupId, @NotNull LocalDate effectiveDate) {}
