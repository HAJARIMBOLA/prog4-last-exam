package com.example.demo.repository;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

  Optional<User> findByEmail(String email);

  boolean existsByMatriculationNumber(String matriculationNumber);

  List<User> findByRole(Role role);
}
