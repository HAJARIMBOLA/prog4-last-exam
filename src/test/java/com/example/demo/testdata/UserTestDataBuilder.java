package com.example.demo.testdata;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import java.util.UUID;

public class UserTestDataBuilder {

  private UUID id = UUID.randomUUID();
  private String email = "s@hei.school";
  private String passwordHash = "hash";
  private Role role = Role.STUDENT;
  private String matriculationNumber = "STD001";
  private String firstName = "Jane";
  private String lastName = "Doe";

  public static UserTestDataBuilder aStudent() {
    return new UserTestDataBuilder();
  }

  public static UserTestDataBuilder aTeacher() {
    return new UserTestDataBuilder()
        .withRole(Role.TEACHER)
        .withEmail("t@hei.school")
        .withMatriculationNumber(null);
  }

  public UserTestDataBuilder withId(UUID id) {
    this.id = id;
    return this;
  }

  public UserTestDataBuilder withEmail(String email) {
    this.email = email;
    return this;
  }

  public UserTestDataBuilder withRole(Role role) {
    this.role = role;
    return this;
  }

  public UserTestDataBuilder withMatriculationNumber(String matriculationNumber) {
    this.matriculationNumber = matriculationNumber;
    return this;
  }

  public UserTestDataBuilder withFirstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public UserTestDataBuilder withLastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public User build() {
    return new User(id, email, passwordHash, role, matriculationNumber, firstName, lastName);
  }
}
