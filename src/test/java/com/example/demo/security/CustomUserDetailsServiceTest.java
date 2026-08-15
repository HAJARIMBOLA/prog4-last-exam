package com.example.demo.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.demo.domain.Role;
import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

  @Mock private UserRepository userRepository;

  @Test
  void loadsAnExistingUserWithItsRoleAsAuthority() {
    var user =
        new User(
            UUID.randomUUID(),
            "t@hei.school",
            "hashed-password",
            Role.TEACHER,
            null,
            "Jane",
            "Doe");
    when(userRepository.findByEmail("t@hei.school")).thenReturn(Optional.of(user));

    var service = new CustomUserDetailsService(userRepository);
    var userDetails = service.loadUserByUsername("t@hei.school");

    assertThat(userDetails.getUsername()).isEqualTo("t@hei.school");
    assertThat(userDetails.getPassword()).isEqualTo("hashed-password");
    assertThat(userDetails.getAuthorities())
        .extracting(Object::toString)
        .containsExactly("ROLE_TEACHER");
  }

  @Test
  void throwsWhenTheUserIsUnknown() {
    when(userRepository.findByEmail("missing@hei.school")).thenReturn(Optional.empty());

    var service = new CustomUserDetailsService(userRepository);

    assertThatThrownBy(() -> service.loadUserByUsername("missing@hei.school"))
        .isInstanceOf(UsernameNotFoundException.class);
  }
}
