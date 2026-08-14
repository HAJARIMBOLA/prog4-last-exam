package com.example.demo.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Course;
import com.example.demo.domain.CourseAssignment;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CourseAssignmentMapperTest {

  @Test
  void mapsEntityToDTO() {
    var course = new Course(UUID.randomUUID(), "ALG101", "Algorithms", 4);
    var teacher =
        new User(UUID.randomUUID(), "t@hei.school", "hash", Role.TEACHER, null, "Jane", "Doe");
    var academicYear = new AcademicYear(UUID.randomUUID(), LocalDate.now(), LocalDate.now());
    var assignment = new CourseAssignment(UUID.randomUUID(), course, teacher, academicYear);

    var dto = CourseAssignmentMapper.toDTO(assignment);

    assertThat(dto.courseId()).isEqualTo(course.getId());
    assertThat(dto.teacherId()).isEqualTo(teacher.getId());
    assertThat(dto.academicYearId()).isEqualTo(academicYear.getId());
  }
}
