package com.example.demo.repository;

import com.example.demo.domain.AcademicYear;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {}
