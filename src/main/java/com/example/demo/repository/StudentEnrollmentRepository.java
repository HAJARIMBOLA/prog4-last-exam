package com.example.demo.repository;

import com.example.demo.domain.StudentEnrollment;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, UUID> {

  Optional<StudentEnrollment> findByStudentIdAndAcademicYearId(UUID studentId, UUID academicYearId);

  List<StudentEnrollment> findByStudentId(UUID studentId);

  List<StudentEnrollment> findByPromotionId(UUID promotionId);

  List<StudentEnrollment> findByAcademicYearId(UUID academicYearId);
}
