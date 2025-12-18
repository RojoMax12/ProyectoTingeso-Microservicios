package com.example.proyectotingeso.Repository;

import com.example.proyectotingeso.Entity.StateToolsEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class StateToolsRepositoryTest {

    @Autowired
    private StateToolsRepository stateToolsRepository;

    @Test
    void whenFindByName_thenReturnStateTools() {
        // given
        StateToolsEntity stateTool = new StateToolsEntity(null, "Available");
        stateToolsRepository.save(stateTool);

        // when
        StateToolsEntity foundStateTool = stateToolsRepository.findByName(stateTool.getName());

        // then
        assertThat(foundStateTool).isNotNull();
        assertThat(foundStateTool.getName()).isEqualTo("Available");
    }
}
