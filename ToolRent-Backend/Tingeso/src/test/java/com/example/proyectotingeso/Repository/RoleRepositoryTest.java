package com.example.proyectotingeso.Repository;

import com.example.proyectotingeso.Entity.RoleEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RoleRepositoryTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void whenFindByName_thenReturnRole() {
        // given
        RoleEntity role = new RoleEntity(null, "ADMIN");
        roleRepository.save(role);

        // when
        RoleEntity foundRole = roleRepository.findByName(role.getName());

        // then
        assertThat(foundRole).isNotNull();
        assertThat(foundRole.getName()).isEqualTo("ADMIN");
    }
}
