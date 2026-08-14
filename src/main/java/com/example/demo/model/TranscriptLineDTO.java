package com.example.demo.model;

import java.util.UUID;

public record TranscriptLineDTO(
    UUID courseId, String courseRef, String courseTitle, UUID examId, Double grade) {}
