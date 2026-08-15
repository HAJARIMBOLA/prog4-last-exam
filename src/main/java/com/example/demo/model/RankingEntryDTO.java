package com.example.demo.model;

import java.util.UUID;

public record RankingEntryDTO(UUID studentId, double average, int rank) {}
