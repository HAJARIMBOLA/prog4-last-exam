package com.example.demo.model;

import java.util.UUID;

public record CourseAssignmentDTO(UUID id, UUID courseId, UUID teacherId, UUID academicYearId) {}
