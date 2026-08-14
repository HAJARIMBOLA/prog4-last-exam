package com.example.demo.endpoint.rest.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.demo.model.GroupMembershipDTO;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtService;
import com.example.demo.service.GroupMembershipService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = GroupMembershipController.class)
@AutoConfigureMockMvc(addFilters = false)
class GroupMembershipControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private GroupMembershipService groupMembershipService;

  @MockBean private JwtService jwtService;

  @MockBean private CustomUserDetailsService userDetailsService;

  @Test
  void changeGroupReturnsCreated() throws Exception {
    var studentId = UUID.randomUUID();
    when(groupMembershipService.changeGroup(any(), any(), any()))
        .thenReturn(
            new GroupMembershipDTO(
                UUID.randomUUID(),
                studentId,
                UUID.randomUUID(),
                "K3",
                LocalDate.of(2026, 1, 1),
                null));

    mockMvc
        .perform(
            post("/students/{id}/group-memberships", studentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"groupId\":\"" + UUID.randomUUID() + "\",\"effectiveDate\":\"2026-01-01\"}"))
        .andExpect(status().isCreated());
  }

  @Test
  void getHistoryReturnsOk() throws Exception {
    when(groupMembershipService.getHistory(any())).thenReturn(List.of());

    mockMvc
        .perform(get("/students/{id}/group-memberships", UUID.randomUUID()))
        .andExpect(status().isOk());
  }
}
