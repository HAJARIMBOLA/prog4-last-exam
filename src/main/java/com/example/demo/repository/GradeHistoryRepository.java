package com.example.demo.repository;

import com.example.demo.domain.GradeHistory;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeHistoryRepository extends JpaRepository<GradeHistory, UUID> {

  List<GradeHistory> findByStudentIdAndExamIdOrderByRecordedAtDesc(UUID studentId, UUID examId);

  List<GradeHistory> findByStudentIdAndExamIdOrderByRecordedAtAsc(UUID studentId, UUID examId);
}
