package com.example.proyectotingeso.Repository;

import com.example.proyectotingeso.Entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void whenFindByEmail_thenReturnUser() {
        // given
        UserEntity user = new UserEntity(null, "Juan Perez", "juan@mail.com", "password", "+569479999", 1L, "12345678-9", 1L);
        userRepository.save(user);

        // when
        UserEntity foundUser = userRepository.findByEmail(user.getEmail());

        // then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("juan@mail.com");
    }
}
