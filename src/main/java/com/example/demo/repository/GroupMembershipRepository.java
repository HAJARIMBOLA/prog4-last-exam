package com.example.demo.repository;

import com.example.demo.domain.GroupMembership;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, UUID> {

  List<GroupMembership> findByStudentIdOrderByDateStartAsc(UUID studentId);

  Optional<GroupMembership> findByStudentIdAndDateEndIsNull(UUID studentId);
}
