package com.example.demo.model;

import com.example.demo.domain.Track;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.UUID;

public record CourseCreationRequest(
    @NotBlank String ref,
    @NotBlank String title,
    @Positive int credits,
    @NotNull UUID academicYearId,
    @NotEmpty List<Track> tracks,
    @NotEmpty List<UUID> teacherIds) {}
