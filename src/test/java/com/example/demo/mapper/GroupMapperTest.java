package com.example.demo.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.Group;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GroupMapperTest {

  @Test
  void mapsEntityToDTO() {
    var group = new Group(UUID.randomUUID(), "K3");
    var dto = GroupMapper.toDTO(group);

    assertThat(dto.ref()).isEqualTo("K3");
  }
}
