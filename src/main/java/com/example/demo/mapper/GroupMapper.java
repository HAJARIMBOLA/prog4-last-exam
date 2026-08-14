package com.example.demo.mapper;

import com.example.demo.domain.Group;
import com.example.demo.model.GroupDTO;

public class GroupMapper {

  private GroupMapper() {}

  public static GroupDTO toDTO(Group group) {
    return new GroupDTO(group.getId(), group.getRef());
  }
}
