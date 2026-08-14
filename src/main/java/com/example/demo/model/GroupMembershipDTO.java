package com.example.demo.model;

import java.time.LocalDate;
import java.util.UUID;

public record GroupMembershipDTO(
    UUID id,
    UUID studentId,
    UUID groupId,
    String groupRef,
    LocalDate dateStart,
    LocalDate dateEnd) {}
