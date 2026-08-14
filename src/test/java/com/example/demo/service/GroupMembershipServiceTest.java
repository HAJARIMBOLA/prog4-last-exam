package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Group;
import com.example.demo.domain.GroupMembership;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.repository.GroupMembershipRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupMembershipServiceTest {

  @Mock private GroupMembershipRepository groupMembershipRepository;
  @Mock private GroupRepository groupRepository;
  @Mock private UserRepository userRepository;

  @Test
  void reconstructsChronologyK1ThenK3ThenK1() {
    var studentId = UUID.randomUUID();
    var student = studentWith(studentId);
    var k1 = groupWith("K1");
    var k3 = groupWith("K3");

    var firstK1 =
        new GroupMembership(
            UUID.randomUUID(), student, k1, LocalDate.of(2024, 9, 1), LocalDate.of(2024, 12, 31));
    var k3Stint =
        new GroupMembership(
            UUID.randomUUID(), student, k3, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 6, 30));
    var secondK1 =
        new GroupMembership(UUID.randomUUID(), student, k1, LocalDate.of(2025, 7, 1), null);

    when(groupMembershipRepository.findByStudentIdOrderByDateStartAsc(studentId))
        .thenReturn(List.of(firstK1, k3Stint, secondK1));

    var service =
        new GroupMembershipService(groupMembershipRepository, groupRepository, userRepository);
    var result =
        service.getGroupsForStudentBetween(
            studentId, LocalDate.of(2024, 9, 1), LocalDate.of(2025, 12, 31));

    assertThat(result).extracting("groupRef").containsExactly("K1", "K3", "K1");
  }

  @Test
  void changingGroupClosesOldMembershipAndOpensNewOneWithoutGapOrOverlap() {
    var studentId = UUID.randomUUID();
    var newGroupId = UUID.randomUUID();
    var student = studentWith(studentId);
    var k1 = groupWith("K1");
    var k3 = new Group(newGroupId, "K3");

    var currentMembership =
        new GroupMembership(UUID.randomUUID(), student, k1, LocalDate.of(2025, 1, 1), null);

    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(groupRepository.findById(newGroupId)).thenReturn(Optional.of(k3));
    when(groupMembershipRepository.findByStudentIdAndDateEndIsNull(studentId))
        .thenReturn(Optional.of(currentMembership));
    when(groupMembershipRepository.save(any(GroupMembership.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var service =
        new GroupMembershipService(groupMembershipRepository, groupRepository, userRepository);
    var effectiveDate = LocalDate.of(2025, 7, 1);
    var newMembership = service.changeGroup(studentId, newGroupId, effectiveDate);

    assertThat(currentMembership.getDateEnd()).isEqualTo(effectiveDate.minusDays(1));
    assertThat(newMembership.dateStart()).isEqualTo(effectiveDate);
    assertThat(newMembership.dateEnd()).isNull();
    assertThat(currentMembership.getDateEnd().plusDays(1)).isEqualTo(newMembership.dateStart());
  }

  @Test
  void changingGroupWithNoPriorMembershipJustOpensANewOne() {
    var studentId = UUID.randomUUID();
    var newGroupId = UUID.randomUUID();
    var student = studentWith(studentId);
    var k1 = new Group(newGroupId, "K1");

    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(groupRepository.findById(newGroupId)).thenReturn(Optional.of(k1));
    when(groupMembershipRepository.findByStudentIdAndDateEndIsNull(studentId))
        .thenReturn(Optional.empty());
    when(groupMembershipRepository.save(any(GroupMembership.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var service =
        new GroupMembershipService(groupMembershipRepository, groupRepository, userRepository);
    var result = service.changeGroup(studentId, newGroupId, LocalDate.of(2024, 9, 1));

    assertThat(result.dateEnd()).isNull();
  }

  private User studentWith(UUID id) {
    return new User(id, "s@hei.school", "hash", Role.STUDENT, "STD001", "Jane", "Doe");
  }

  private Group groupWith(String ref) {
    return new Group(UUID.randomUUID(), ref);
  }
}
