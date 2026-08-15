package com.example.demo.repository;

import com.example.demo.domain.AcademicYear;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {

  Optional<AcademicYear> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
      LocalDate startBound, LocalDate endBound);
}
