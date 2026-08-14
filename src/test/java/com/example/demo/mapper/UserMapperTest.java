package com.example.demo.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserMapperTest {

  @Test
  void mappedDTONeverExposesPasswordHash() {
    var user =
        new User(
            UUID.randomUUID(), "a@b.com", "secret-hash", Role.STUDENT, "STD001", "Jane", "Doe");
    var dto = UserMapper.toDTO(user);

    assertThat(dto.toString()).doesNotContain("secret-hash");
  }
}
