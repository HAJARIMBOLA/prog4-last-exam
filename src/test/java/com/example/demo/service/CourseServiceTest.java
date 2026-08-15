package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.domain.AcademicYear;
import com.example.demo.domain.Course;
import com.example.demo.domain.CourseAssignment;
import com.example.demo.domain.CourseTrack;
import com.example.demo.domain.Role;
import com.example.demo.domain.Semester;
import com.example.demo.domain.Track;
import com.example.demo.domain.User;
import com.example.demo.model.CourseCreationRequest;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.CourseAssignmentRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.CourseTrackRepository;
import com.example.demo.repository.SemesterRepository;
import com.example.demo.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

  @Mock private CourseRepository courseRepository;
  @Mock private CourseTrackRepository courseTrackRepository;
  @Mock private CourseAssignmentRepository courseAssignmentRepository;
  @Mock private UserRepository userRepository;
  @Mock private AcademicYearRepository academicYearRepository;
  @Mock private SemesterRepository semesterRepository;
  @Mock private CreditStructureValidationService creditStructureValidationService;

  @Test
  void createCourseWithOneTeacherSavesOneAssignment() {
    var courseService =
        new CourseService(
            courseRepository,
            courseTrackRepository,
            courseAssignmentRepository,
            userRepository,
            academicYearRepository,
            semesterRepository,
            creditStructureValidationService);

    var academicYearId = UUID.randomUUID();
    var semesterId = UUID.randomUUID();
    var teacherId = UUID.randomUUID();
    var course = new Course(UUID.randomUUID(), "ALG101", "Algorithms", 4);
    var teacher = new User(teacherId, "t@hei.school", "hash", Role.TEACHER, null, "Jane", "Doe");
    var academicYear = new AcademicYear(academicYearId, LocalDate.now(), LocalDate.now());
    var semester = new Semester(semesterId, academicYear, 1, LocalDate.now(), LocalDate.now());

    when(courseRepository.save(any(Course.class))).thenReturn(course);
    when(courseTrackRepository.save(any(CourseTrack.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(academicYearRepository.findById(academicYearId)).thenReturn(Optional.of(academicYear));
    when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(semester));
    when(userRepository.findById(teacherId)).thenReturn(Optional.of(teacher));
    when(courseAssignmentRepository.save(any(CourseAssignment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var request =
        new CourseCreationRequest(
            "ALG101",
            "Algorithms",
            4,
            academicYearId,
            semesterId,
            List.of(Track.COMMON_CORE),
            List.of(teacherId));

    var result = courseService.createCourse(request);

    assertThat(result.ref()).isEqualTo("ALG101");
    verify(courseAssignmentRepository, times(1)).save(any(CourseAssignment.class));
  }

  @Test
  void createCourseWithSeveralTeachersSavesSeveralAssignmentsWithoutConflict() {
    var courseService =
        new CourseService(
            courseRepository,
            courseTrackRepository,
            courseAssignmentRepository,
            userRepository,
            academicYearRepository,
            semesterRepository,
            creditStructureValidationService);

    var academicYearId = UUID.randomUUID();
    var semesterId = UUID.randomUUID();
    var teacherId1 = UUID.randomUUID();
    var teacherId2 = UUID.randomUUID();
    var course = new Course(UUID.randomUUID(), "ALG101", "Algorithms", 4);
    var teacher1 = new User(teacherId1, "t1@hei.school", "hash", Role.TEACHER, null, "Jane", "Doe");
    var teacher2 =
        new User(teacherId2, "t2@hei.school", "hash", Role.TEACHER, null, "John", "Smith");
    var academicYear = new AcademicYear(academicYearId, LocalDate.now(), LocalDate.now());
    var semester = new Semester(semesterId, academicYear, 1, LocalDate.now(), LocalDate.now());

    when(courseRepository.save(any(Course.class))).thenReturn(course);
    when(courseTrackRepository.save(any(CourseTrack.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
    when(academicYearRepository.findById(academicYearId)).thenReturn(Optional.of(academicYear));
    when(semesterRepository.findById(semesterId)).thenReturn(Optional.of(semester));
    when(userRepository.findById(teacherId1)).thenReturn(Optional.of(teacher1));
    when(userRepository.findById(teacherId2)).thenReturn(Optional.of(teacher2));
    when(courseAssignmentRepository.save(any(CourseAssignment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    var request =
        new CourseCreationRequest(
            "ALG101",
            "Algorithms",
            4,
            academicYearId,
            semesterId,
            List.of(Track.EL, Track.TN),
            List.of(teacherId1, teacherId2));

    courseService.createCourse(request);

    verify(courseAssignmentRepository, times(2)).save(any(CourseAssignment.class));
    verify(courseTrackRepository, times(2)).save(any(CourseTrack.class));
  }
}
