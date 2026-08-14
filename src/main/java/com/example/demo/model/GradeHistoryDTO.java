package com.example.demo.model;

import java.time.Instant;
import java.util.UUID;

public record GradeHistoryDTO(
    UUID id, UUID studentId, UUID examId, double value, Instant recordedAt, UUID recordedById) {}
