package com.example.demo.model;

import com.example.demo.domain.Track;
import java.util.UUID;

public record CourseTrackDTO(UUID id, UUID courseId, Track track, UUID semesterId) {}
