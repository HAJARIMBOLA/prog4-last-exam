package com.example.demo.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.model.CreateStudentRequest;
import com.example.demo.model.CreateTeacherRequest;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private MatriculationNumberValidator matriculationNumberValidator;

  @Test
  void createStudentAssignsStudentRoleAndHashedTemporaryPassword() {
    when(passwordEncoder.encode(org.mockito.ArgumentMatchers.anyString())).thenReturn("hashed");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var service =
        new UserAdminService(userRepository, passwordEncoder, matriculationNumberValidator);
    var response =
        service.createStudent(new CreateStudentRequest("s@hei.school", "Jane", "Doe", "STD042"));

    verify(matriculationNumberValidator).validate("STD042");
    assertThat(response.user().role()).isEqualTo(Role.STUDENT);
    assertThat(response.temporaryPassword()).isNotBlank();
    assertThat(response.temporaryPassword()).isNotEqualTo("hashed");

    var captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    assertThat(captor.getValue().getPasswordHash()).isEqualTo("hashed");
  }

  @Test
  void createTeacherAssignsTeacherRoleAndNeverValidatesMatriculationNumber() {
    when(passwordEncoder.encode(org.mockito.ArgumentMatchers.anyString())).thenReturn("hashed");
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    var service =
        new UserAdminService(userRepository, passwordEncoder, matriculationNumberValidator);
    var response = service.createTeacher(new CreateTeacherRequest("t@hei.school", "John", "Smith"));

    assertThat(response.user().role()).isEqualTo(Role.TEACHER);
    verify(matriculationNumberValidator, never())
        .validate(org.mockito.ArgumentMatchers.anyString());
  }
}
