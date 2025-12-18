package com.example.proyectotingeso.Repository;

import com.example.proyectotingeso.Entity.ClientEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ClientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ClientRepository clientRepository;

    @Test
    void whenFindByRut_thenReturnClient() {
        // given
        ClientEntity client = new ClientEntity(null, "Juan Perez", "juan@mail.com", "12345678-9", "555-1234", 1L);
        entityManager.persistAndFlush(client);

        // when
        ClientEntity found = clientRepository.findByRut(client.getRut());

        // then
        assertThat(found).isNotNull();
        assertThat(found.getRut()).isEqualTo("12345678-9");
        assertThat(found.getName()).isEqualTo("Juan Perez");
    }
}