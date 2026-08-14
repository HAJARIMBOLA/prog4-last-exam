package com.example.demo.model;

import com.example.demo.domain.Track;
import java.util.UUID;

public record StudentEnrollmentDTO(
    UUID id,
    UUID studentId,
    UUID promotionId,
    UUID academicYearId,
    Track trackAtTime,
    boolean repeating) {}
