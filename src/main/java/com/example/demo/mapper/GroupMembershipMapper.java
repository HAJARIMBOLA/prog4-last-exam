package com.example.demo.mapper;

import com.example.demo.domain.GroupMembership;
import com.example.demo.model.GroupMembershipDTO;

public class GroupMembershipMapper {

  private GroupMembershipMapper() {}

  public static GroupMembershipDTO toDTO(GroupMembership membership) {
    return new GroupMembershipDTO(
        membership.getId(),
        membership.getStudent().getId(),
        membership.getGroup().getId(),
        membership.getGroup().getRef(),
        membership.getDateStart(),
        membership.getDateEnd());
  }
}
