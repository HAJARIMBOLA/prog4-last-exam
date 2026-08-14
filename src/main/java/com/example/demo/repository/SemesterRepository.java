package com.example.demo.repository;

import com.example.demo.domain.Semester;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SemesterRepository extends JpaRepository<Semester, UUID> {

  List<Semester> findByAcademicYearId(UUID academicYearId);
}
