package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Course;
import com.example.demo.domain.CourseAssignment;
import com.example.demo.domain.Exam;
import com.example.demo.domain.GradeHistory;
import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.CourseAssignmentRepository;
import com.example.demo.repository.ExamRepository;
import com.example.demo.repository.GradeHistoryRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class GradeSubmissionServiceTest {

  @Mock private GradeHistoryRepository gradeHistoryRepository;
  @Mock private ExamRepository examRepository;
  @Mock private UserRepository userRepository;
  @Mock private AcademicYearRepository academicYearRepository;
  @Mock private CourseAssignmentRepository courseAssignmentRepository;

  @Test
  void assignedTeacherCanRecordAValidGrade() {
    var examId = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var course = new Course(UUID.randomUUID(), "ALG101", "Algorithms", 4);
    var examDate = LocalDate.of(2026, 3, 1);
    var exam = new Exam(examId, examDate, 0.5, course);
    var student =
        new User(studentId, "s@hei.school", "hash", Role.STUDENT, "STD001", "Jane", "Doe");
    var teacher =
        new User(UUID.randomUUID(), "t@hei.school", "hash", Role.TEACHER, null, "John", "Smith");
    var academicYear =
        new AcademicYear(UUID.randomUUID(), LocalDate.of(2025, 9, 1), LocalDate.of(2026, 7, 1));
    var assignment = new CourseAssignment(UUID.randomUUID(), course, teacher, academicYear);

    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(userRepository.findByEmail("t@hei.school")).thenReturn(Optional.of(teacher));
    when(academicYearRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            examDate, examDate))
        .thenReturn(Optional.of(academicYear));
    when(courseAssignmentRepository.findByCourseIdAndAcademicYearId(
            course.getId(), academicYear.getId()))
        .thenReturn(List.of(assignment));
    when(gradeHistoryRepository.save(any(GradeHistory.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var service =
        new GradeSubmissionService(
            gradeHistoryRepository,
            examRepository,
            userRepository,
            academicYearRepository,
            courseAssignmentRepository);
    var result = service.recordGrade(examId, studentId, 15.0, "t@hei.school");

    assertThat(result.value()).isEqualTo(15.0);
  }

  @Test
  void unassignedTeacherIsRejectedWithAccessDenied() {
    var examId = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var course = new Course(UUID.randomUUID(), "ALG101", "Algorithms", 4);
    var examDate = LocalDate.of(2026, 3, 1);
    var exam = new Exam(examId, examDate, 0.5, course);
    var student =
        new User(studentId, "s@hei.school", "hash", Role.STUDENT, "STD001", "Jane", "Doe");
    var otherTeacher =
        new User(UUID.randomUUID(), "other@hei.school", "hash", Role.TEACHER, null, "Ann", "Lee");
    var unassignedTeacher =
        new User(
            UUID.randomUUID(), "unassigned@hei.school", "hash", Role.TEACHER, null, "Bob", "Fox");
    var academicYear =
        new AcademicYear(UUID.randomUUID(), LocalDate.of(2025, 9, 1), LocalDate.of(2026, 7, 1));
    var assignment = new CourseAssignment(UUID.randomUUID(), course, otherTeacher, academicYear);

    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(userRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(userRepository.findByEmail("unassigned@hei.school"))
        .thenReturn(Optional.of(unassignedTeacher));
    when(academicYearRepository.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            examDate, examDate))
        .thenReturn(Optional.of(academicYear));
    when(courseAssignmentRepository.findByCourseIdAndAcademicYearId(
            course.getId(), academicYear.getId()))
        .thenReturn(List.of(assignment));

    var service =
        new GradeSubmissionService(
            gradeHistoryRepository,
            examRepository,
            userRepository,
            academicYearRepository,
            courseAssignmentRepository);

    assertThatThrownBy(() -> service.recordGrade(examId, studentId, 15.0, "unassigned@hei.school"))
        .isInstanceOf(AccessDeniedException.class);
  }
}
