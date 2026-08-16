package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Course;
import com.example.demo.domain.CourseAssignment;
import com.example.demo.domain.CourseTrack;
import com.example.demo.domain.Track;
import com.example.demo.model.CourseCreationRequest;
import com.example.demo.repository.AcademicYearRepository;
import com.example.demo.repository.CourseAssignmentRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.CourseTrackRepository;
import com.example.demo.repository.SemesterRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.testdata.AcademicYearTestDataBuilder;
import com.example.demo.testdata.CourseTestDataBuilder;
import com.example.demo.testdata.UserTestDataBuilder;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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
    var teacherId = UUID.randomUUID();
    var course = CourseTestDataBuilder.aCourse().build();
    var teacher = UserTestDataBuilder.aTeacher().withId(teacherId).build();
    var academicYear = AcademicYearTestDataBuilder.anAcademicYear().withId(academicYearId).build();
    var semester =
        AcademicYearTestDataBuilder.anAcademicYear().withId(academicYearId).buildSemester(1);
    var semesterId = semester.getId();

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
    var teacherId1 = UUID.randomUUID();
    var teacherId2 = UUID.randomUUID();
    var course = CourseTestDataBuilder.aCourse().build();
    var teacher1 =
        UserTestDataBuilder.aTeacher().withId(teacherId1).withEmail("t1@hei.school").build();
    var teacher2 =
        UserTestDataBuilder.aTeacher()
            .withId(teacherId2)
            .withEmail("t2@hei.school")
            .withFirstName("John")
            .withLastName("Smith")
            .build();
    var academicYear = AcademicYearTestDataBuilder.anAcademicYear().withId(academicYearId).build();
    var semester =
        AcademicYearTestDataBuilder.anAcademicYear().withId(academicYearId).buildSemester(1);
    var semesterId = semester.getId();

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

  @Test
  void listCoursesPaginatedReportsTheCorrectPageAndTotalCount() {
    var courseService =
        new CourseService(
            courseRepository,
            courseTrackRepository,
            courseAssignmentRepository,
            userRepository,
            academicYearRepository,
            semesterRepository,
            creditStructureValidationService);

    var course = CourseTestDataBuilder.aCourse().build();
    var pageable = PageRequest.of(0, 1);
    when(courseRepository.findAll(pageable))
        .thenReturn(new PageImpl<>(List.of(course), pageable, 3));

    var result = courseService.listCourses(pageable);

    assertThat(result.content()).hasSize(1);
    assertThat(result.page()).isEqualTo(0);
    assertThat(result.size()).isEqualTo(1);
    assertThat(result.totalElements()).isEqualTo(3);
    assertThat(result.totalPages()).isEqualTo(3);
  }
}
