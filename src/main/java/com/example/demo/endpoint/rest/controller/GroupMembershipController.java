package com.example.demo.endpoint.rest.controller;

import com.example.demo.model.ChangeGroupRequest;
import com.example.demo.model.GroupMembershipDTO;
import com.example.demo.service.GroupMembershipService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class GroupMembershipController {

  private final GroupMembershipService groupMembershipService;

  @PostMapping("/students/{id}/group-memberships")
  public ResponseEntity<GroupMembershipDTO> changeGroup(
      @PathVariable UUID id, @Valid @RequestBody ChangeGroupRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(groupMembershipService.changeGroup(id, request.groupId(), request.effectiveDate()));
  }

  @GetMapping("/students/{id}/group-memberships")
  public ResponseEntity<List<GroupMembershipDTO>> getHistory(@PathVariable UUID id) {
    return ResponseEntity.ok(groupMembershipService.getHistory(id));
  }
}
