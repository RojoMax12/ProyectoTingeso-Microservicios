package com.example.proyectotingeso.Repository;

import com.example.proyectotingeso.Entity.StateUsersEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class StateUsersRepositoryTest {

    @Autowired
    private StateUsersRepository stateUsersRepository;

    @Test
    void whenFindByName_thenReturnStateUser() {
        // given
        StateUsersEntity stateUser = new StateUsersEntity(null, "Active");
        stateUsersRepository.save(stateUser);

        // when
        StateUsersEntity foundStateUser = stateUsersRepository.findByName(stateUser.getName());

        // then
        assertThat(foundStateUser).isNotNull();
        assertThat(foundStateUser.getName()).isEqualTo("Active");
    }
}
