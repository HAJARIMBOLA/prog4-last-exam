package com.example.demo.service;

import com.example.demo.domain.GroupMembership;
import com.example.demo.exception.NotFoundException;
import com.example.demo.mapper.GroupMembershipMapper;
import com.example.demo.model.GroupMembershipDTO;
import com.example.demo.repository.GroupMembershipRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class GroupMembershipService {

  private final GroupMembershipRepository groupMembershipRepository;
  private final GroupRepository groupRepository;
  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public List<GroupMembershipDTO> getGroupsForStudentBetween(
      UUID studentId, LocalDate start, LocalDate end) {
    return groupMembershipRepository.findByStudentIdOrderByDateStartAsc(studentId).stream()
        .filter(
            membership ->
                !membership.getDateStart().isAfter(end)
                    && (membership.getDateEnd() == null
                        || !membership.getDateEnd().isBefore(start)))
        .map(GroupMembershipMapper::toDTO)
        .toList();
  }

  public GroupMembershipDTO changeGroup(UUID studentId, UUID newGroupId, LocalDate effectiveDate) {
    var student =
        userRepository
            .findById(studentId)
            .orElseThrow(() -> new NotFoundException("Student not found: " + studentId));
    var newGroup =
        groupRepository
            .findById(newGroupId)
            .orElseThrow(() -> new NotFoundException("Group not found: " + newGroupId));

    groupMembershipRepository
        .findByStudentIdAndDateEndIsNull(studentId)
        .ifPresent(
            current -> {
              current.setDateEnd(effectiveDate.minusDays(1));
              groupMembershipRepository.save(current);
            });

    var membership = new GroupMembership();
    membership.setStudent(student);
    membership.setGroup(newGroup);
    membership.setDateStart(effectiveDate);
    membership.setDateEnd(null);

    return GroupMembershipMapper.toDTO(groupMembershipRepository.save(membership));
  }

  @Transactional(readOnly = true)
  public List<GroupMembershipDTO> getHistory(UUID studentId) {
    return groupMembershipRepository.findByStudentIdOrderByDateStartAsc(studentId).stream()
        .map(GroupMembershipMapper::toDTO)
        .toList();
  }
}
