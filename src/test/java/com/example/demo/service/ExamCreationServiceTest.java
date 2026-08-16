package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Course;
import com.example.demo.domain.Exam;
import com.example.demo.exception.InvalidCoefficientSumException;
import com.example.demo.model.ExamCreationRequest;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.ExamRepository;
import com.example.demo.testdata.CourseTestDataBuilder;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExamCreationServiceTest {

  @Mock private ExamRepository examRepository;
  @Mock private CourseRepository courseRepository;

  @Test
  void completingTheCoefficientSumToExactlyOneIsAccepted() {
    var courseId = UUID.randomUUID();
    var course = CourseTestDataBuilder.aCourse().withId(courseId).build();
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
    when(examRepository.findByCourseId(courseId))
        .thenReturn(List.of(examWith(course, 0.6)))
        .thenReturn(List.of(examWith(course, 0.6), examWith(course, 0.4)));
    when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var service = new ExamCreationService(examRepository, courseRepository);
    var result = service.createExam(courseId, new ExamCreationRequest(LocalDate.now(), 0.4));

    assertThat(result.coefficient()).isEqualTo(0.4);
    assertThat(service.isCourseComplete(courseId)).isTrue();
  }

  @Test
  void leavingTheCoefficientSumBelowOneIsAcceptedButIncomplete() {
    var courseId = UUID.randomUUID();
    var course = CourseTestDataBuilder.aCourse().withId(courseId).build();
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
    when(examRepository.findByCourseId(courseId)).thenReturn(List.of(examWith(course, 0.3)));
    when(examRepository.save(any(Exam.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var service = new ExamCreationService(examRepository, courseRepository);
    service.createExam(courseId, new ExamCreationRequest(LocalDate.now(), 0.3));

    assertThat(service.isCourseComplete(courseId)).isFalse();
  }

  @Test
  void exceedingACoefficientSumOfOneIsRejected() {
    var courseId = UUID.randomUUID();
    var course = CourseTestDataBuilder.aCourse().withId(courseId).build();
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));
    when(examRepository.findByCourseId(courseId)).thenReturn(List.of(examWith(course, 0.7)));

    var service = new ExamCreationService(examRepository, courseRepository);

    assertThatThrownBy(
            () -> service.createExam(courseId, new ExamCreationRequest(LocalDate.now(), 0.5)))
        .isInstanceOf(InvalidCoefficientSumException.class);
  }

  private Exam examWith(Course course, double coefficient) {
    return new Exam(UUID.randomUUID(), LocalDate.now(), coefficient, course);
  }
}
