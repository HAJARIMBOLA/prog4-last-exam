package com.example.demo.mapper;

import com.example.demo.domain.User;
import com.example.demo.model.UserDTO;

public class UserMapper {

  private UserMapper() {}

  public static UserDTO toDTO(User user) {
    return new UserDTO(
        user.getId(),
        user.getEmail(),
        user.getRole(),
        user.getMatriculationNumber(),
        user.getFirstName(),
        user.getLastName());
  }
}
