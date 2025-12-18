package com.example.proyectotingeso.Repository;

import com.example.proyectotingeso.Entity.ToolEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ToolRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ToolRepository toolRepository;

    @Test
    void whenFindByName_thenReturnTool() {
        // given
        ToolEntity tool = new ToolEntity(null, "Naty Martillo", "Martillo", 20000, 2L);
        entityManager.persistAndFlush(tool);

        // when
        ToolEntity found = toolRepository.findByName(tool.getName());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("Naty Martillo");
    }

    @Test
    void whenFindById_thenReturnTool() {
        // given
        ToolEntity tool = new ToolEntity(null, "Naty Martillo", "Martillo", 20000, 2L);
        entityManager.persistAndFlush(tool);

        // when
        ToolEntity found = toolRepository.findById(tool.getId()).orElse(null);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(tool.getId());
    }
}
