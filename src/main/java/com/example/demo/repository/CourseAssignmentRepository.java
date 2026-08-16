package com.example.demo.repository;

import com.example.demo.domain.CourseAssignment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, UUID> {

  List<CourseAssignment> findByCourseIdAndAcademicYearId(UUID courseId, UUID academicYearId);

  List<CourseAssignment> findByCourseId(UUID courseId);
}
