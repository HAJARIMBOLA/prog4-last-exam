package com.example.demo.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AcademicYearDTO(
    UUID id, LocalDate startDate, LocalDate endDate, List<SemesterDTO> semesters) {}
