package com.example.demo.model;

import java.time.LocalDate;
import java.util.UUID;

public record SemesterDTO(
    UUID id, UUID academicYearId, int semesterNumber, LocalDate startDate, LocalDate endDate) {}
