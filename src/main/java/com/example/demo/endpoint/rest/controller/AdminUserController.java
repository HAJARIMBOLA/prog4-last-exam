package com.example.demo.endpoint.rest.controller;

import com.example.demo.model.CreateStudentRequest;
import com.example.demo.model.CreateTeacherRequest;
import com.example.demo.model.UpdateStudentRequest;
import com.example.demo.model.UpdateTeacherRequest;
import com.example.demo.model.UserCreationResponse;
import com.example.demo.model.UserDTO;
import com.example.demo.service.UserAdminService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class AdminUserController {

  private final UserAdminService userAdminService;

  @PostMapping("/admin/students")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserCreationResponse> createStudent(
      @Valid @RequestBody CreateStudentRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userAdminService.createStudent(request));
  }

  @PutMapping("/admin/students/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDTO> updateStudent(
      @PathVariable UUID id, @Valid @RequestBody UpdateStudentRequest request) {
    return ResponseEntity.ok(userAdminService.updateStudent(id, request));
  }

  @PostMapping("/admin/teachers")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserCreationResponse> createTeacher(
      @Valid @RequestBody CreateTeacherRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userAdminService.createTeacher(request));
  }

  @PutMapping("/admin/teachers/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserDTO> updateTeacher(
      @PathVariable UUID id, @Valid @RequestBody UpdateTeacherRequest request) {
    return ResponseEntity.ok(userAdminService.updateTeacher(id, request));
  }
}
