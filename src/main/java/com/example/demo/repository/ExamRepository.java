package com.example.demo.repository;

import com.example.demo.domain.Exam;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamRepository extends JpaRepository<Exam, UUID> {}
