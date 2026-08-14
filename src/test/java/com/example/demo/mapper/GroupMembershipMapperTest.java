package com.example.demo.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Group;
import com.example.demo.domain.GroupMembership;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GroupMembershipMapperTest {

  @Test
  void mapsEntityToDTOIncludingGroupRef() {
    var student =
        new User(UUID.randomUUID(), "s@hei.school", "hash", Role.STUDENT, "STD001", "Jane", "Doe");
    var group = new Group(UUID.randomUUID(), "K2");
    var membership =
        new GroupMembership(UUID.randomUUID(), student, group, LocalDate.of(2026, 1, 1), null);

    var dto = GroupMembershipMapper.toDTO(membership);

    assertThat(dto.studentId()).isEqualTo(student.getId());
    assertThat(dto.groupId()).isEqualTo(group.getId());
    assertThat(dto.groupRef()).isEqualTo("K2");
    assertThat(dto.dateEnd()).isNull();
  }
}
