package com.example.proyectotingeso.Repository;

import com.example.proyectotingeso.Entity.AmountsandratesEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class AmountsandratesRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AmountsandratesRepository amountsandratesRepository;

    @Test
    void whenFindById_thenReturnAmountsandrates() {
        // given
        AmountsandratesEntity entity = new AmountsandratesEntity(null, 1000.0, 50.0, 200.0);
        entityManager.persistAndFlush(entity);

        // when
        Optional<AmountsandratesEntity> found = amountsandratesRepository.findById(entity.getId());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getDailyrentalrate()).isEqualTo(1000.0);
    }

    @Test
    void whenFindByDailyrentalrateAndDailylatefeefineAndReparationcharge_thenReturnAmountsandrates() {
        // given
        AmountsandratesEntity entity = new AmountsandratesEntity(null, 1200.0, 60.0, 300.0);
        entityManager.persistAndFlush(entity);

        // when
        AmountsandratesEntity found = amountsandratesRepository
                .findByDailyrentalrateAndDailylatefeefineAndReparationcharge(1200.0, 60.0, 300.0);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getDailyrentalrate()).isEqualTo(1200.0);
        assertThat(found.getDailylatefeefine()).isEqualTo(60.0);
        assertThat(found.getReparationcharge()).isEqualTo(300.0);
    }
}
