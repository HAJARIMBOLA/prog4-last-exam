package com.example.demo.endpoint.rest.controller;

import com.example.demo.model.CourseCreationRequest;
import com.example.demo.model.CourseDTO;
import com.example.demo.service.CourseService;
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
public class CourseController {

  private final CourseService courseService;

  @PostMapping("/courses")
  public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseCreationRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(request));
  }

  @GetMapping("/courses")
  public ResponseEntity<List<CourseDTO>> listCourses() {
    return ResponseEntity.ok(courseService.listCourses());
  }

  @GetMapping("/courses/{id}")
  public ResponseEntity<CourseDTO> getCourse(@PathVariable UUID id) {
    return courseService
        .findCourse(id)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
