package com.example.demo.service;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.exception.NotFoundException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.CreateStudentRequest;
import com.example.demo.model.CreateTeacherRequest;
import com.example.demo.model.UpdateStudentRequest;
import com.example.demo.model.UpdateTeacherRequest;
import com.example.demo.model.UserCreationResponse;
import com.example.demo.model.UserDTO;
import com.example.demo.repository.UserRepository;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserAdminService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final MatriculationNumberValidator matriculationNumberValidator;

  public UserCreationResponse createStudent(CreateStudentRequest request) {
    matriculationNumberValidator.validate(request.matriculationNumber());

    var temporaryPassword = TemporaryPasswordGenerator.generate();
    var user = new User();
    user.setEmail(request.email());
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setMatriculationNumber(request.matriculationNumber());
    user.setRole(Role.STUDENT);
    user.setPasswordHash(passwordEncoder.encode(temporaryPassword));

    return new UserCreationResponse(UserMapper.toDTO(userRepository.save(user)), temporaryPassword);
  }

  public UserCreationResponse createTeacher(CreateTeacherRequest request) {
    var temporaryPassword = TemporaryPasswordGenerator.generate();
    var user = new User();
    user.setEmail(request.email());
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setRole(Role.TEACHER);
    user.setPasswordHash(passwordEncoder.encode(temporaryPassword));

    return new UserCreationResponse(UserMapper.toDTO(userRepository.save(user)), temporaryPassword);
  }

  public UserDTO updateStudent(UUID id, UpdateStudentRequest request) {
    var user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Student not found: " + id));
    user.setEmail(request.email());
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    user.setMatriculationNumber(request.matriculationNumber());
    return UserMapper.toDTO(userRepository.save(user));
  }

  public UserDTO updateTeacher(UUID id, UpdateTeacherRequest request) {
    var user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Teacher not found: " + id));
    user.setEmail(request.email());
    user.setFirstName(request.firstName());
    user.setLastName(request.lastName());
    return UserMapper.toDTO(userRepository.save(user));
  }
}
