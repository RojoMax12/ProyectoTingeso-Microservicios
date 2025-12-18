package com.example.proyectotingeso.Repository;

import static org.junit.jupiter.api.Assertions.*;

import com.example.proyectotingeso.Entity.LoanToolsEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

// Anotación clave para pruebas de Repositorios JPA
@DataJpaTest
@ActiveProfiles("test")
public class LoanToolsRepositoryTest {

    @Autowired
    private LoanToolsRepository loanToolsRepository;

    @Autowired
    private TestEntityManager entityManager; // Para inicializar datos

    // Fechas de prueba
    private final LocalDate today = LocalDate.now();
    private final LocalDate yesterday = today.minusDays(1);

    // Entidades de prueba
    private LoanToolsEntity loan1;
    private LoanToolsEntity loan2;
    private LoanToolsEntity loan3;
    private LoanToolsEntity loan4;
    private LoanToolsEntity loan5;

    @BeforeEach
    void setup() {
        // Limpiar y configurar entidades antes de cada test

        // Préstamo 1: Cliente 1, Herramienta 1, Activo, RentalFee 50.0
        loan1 = new LoanToolsEntity(null, yesterday, today, 1L, 1L, "Active", 0.0, 50.0, 0.0, 0.0);
        entityManager.persistAndFlush(loan1);

        // Préstamo 2: Cliente 1, Herramienta 2, Late, RentalFee 10.0
        loan2 = new LoanToolsEntity(null, yesterday.minusDays(5), yesterday, 1L, 2L, "Late", 10.0, 10.0, 0.0, 0.0);
        entityManager.persistAndFlush(loan2);

        // Préstamo 3: Cliente 2, Herramienta 3, Active, RentalFee 60.0
        loan3 = new LoanToolsEntity(null, yesterday, today, 2L, 3L, "Active", 0.0, 60.0, 0.0, 0.0);
        entityManager.persistAndFlush(loan3);

        // Préstamo 4: Cliente 2, Herramienta 4, Returned, RentalFee 50.0
        loan4 = new LoanToolsEntity(null, yesterday.minusDays(10), yesterday.minusDays(5), 2L, 4L, "Returned", 0.0, 50.0, 0.0, 0.0);
        entityManager.persistAndFlush(loan4);

        // Préstamo 5: Cliente 3, Herramienta 5, Late, RentalFee 5.0 (Menor que 10)
        loan5 = new LoanToolsEntity(null, yesterday.minusDays(5), yesterday, 3L, 5L, "Late", 5.0, 5.0, 0.0, 0.0);
        entityManager.persistAndFlush(loan5);
    }

    // =========================================================================
    // 1. Tests para findById(Long id)
    // =========================================================================

    // (Este método se hereda de JpaRepository, pero lo incluimos para integridad)
    @Test
    void testFindById_LoanExists_ReturnsLoan() {
        // Act
        Optional<LoanToolsEntity> found = loanToolsRepository.findById(loan1.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals(1L, found.get().getClientid());
    }

    // =========================================================================
    // 2. Tests para findByClientidAndToolid(Long clientid, Long toolid)
    // =========================================================================

    @Test
    void testFindByClientidAndToolid_LoanExists_ReturnsLoan() {
        // Act
        Optional<LoanToolsEntity> found = loanToolsRepository.findByClientidAndToolid(1L, 1L);

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Active", found.get().getStatus());
    }

    @Test
    void testFindByClientidAndToolid_LoanNotFound_ReturnsEmpty() {
        // Act
        Optional<LoanToolsEntity> found = loanToolsRepository.findByClientidAndToolid(99L, 99L);

        // Assert
        assertFalse(found.isPresent());
    }

    // =========================================================================
    // 3. Tests para findAllByClientid(Long clientid)
    // =========================================================================

    @Test
    void testFindAllByClientid_ClientHasMultipleLoans_ReturnsAll() {
        // Act
        List<LoanToolsEntity> loans = loanToolsRepository.findAllByClientid(1L);

        // Assert
        assertEquals(2, loans.size());
        assertTrue(loans.stream().anyMatch(l -> "Late".equals(l.getStatus())));
    }

    @Test
    void testFindAllByClientid_ClientHasNoLoans_ReturnsEmptyList() {
        // Act
        List<LoanToolsEntity> loans = loanToolsRepository.findAllByClientid(99L);

        // Assert
        assertTrue(loans.isEmpty());
    }

    // =========================================================================
    // 4. Tests para findAllBystatusInAndRentalFeeGreaterThan(Collection<String> statuses, double rentalFee)
    // =========================================================================

    @Test
    void testFindAllBystatusInAndRentalFeeGreaterThan_ReturnsFilteredLoans() {
        // Arrange
        Collection<String> targetStatuses = Arrays.asList("Active", "Late");
        double minRentalFee = 40.0; // Filtra (loan1: 50, loan2: 10, loan3: 60, loan5: 5)

        // Esperados: loan1 (Active, 50.0) y loan3 (Active, 60.0)

        // Act
        List<LoanToolsEntity> loans = loanToolsRepository.findAllBystatusInAndRentalFeeGreaterThan(targetStatuses, minRentalFee);

        // Assert
        assertEquals(2, loans.size());
        // Verificamos que solo están los que cumplen ambas condiciones
        assertTrue(loans.stream().allMatch(l -> l.getRentalFee() > minRentalFee));
        assertTrue(loans.stream().anyMatch(l -> l.getToolid() == 1L));
        assertTrue(loans.stream().anyMatch(l -> l.getToolid() == 3L));
        assertFalse(loans.stream().anyMatch(l -> l.getToolid() == 2L)); // Loan2 (Late, 10.0) excluido por Fee
    }

    @Test
    void testFindAllBystatusInAndRentalFeeGreaterThan_NoMatch_ReturnsEmptyList() {
        // Arrange
        Collection<String> targetStatuses = Arrays.asList("Returned");
        double minRentalFee = 100.0;

        // Act
        List<LoanToolsEntity> loans = loanToolsRepository.findAllBystatusInAndRentalFeeGreaterThan(targetStatuses, minRentalFee);

        // Assert
        assertTrue(loans.isEmpty());
    }

    // =========================================================================
    // 5. Tests para findAllBystatus(String status)
    // =========================================================================

    @Test
    void testFindAllBystatus_Active_ReturnsTwoLoans() {
        // Act
        List<LoanToolsEntity> loans = loanToolsRepository.findAllBystatus("Active");

        // Assert
        assertEquals(2, loans.size());
        assertTrue(loans.stream().allMatch(l -> "Active".equals(l.getStatus())));
    }

    @Test
    void testFindAllBystatus_Late_ReturnsTwoLoans() {
        // Act
        List<LoanToolsEntity> loans = loanToolsRepository.findAllBystatus("Late");

        // Assert
        assertEquals(2, loans.size()); // loan2 y loan5
        assertTrue(loans.stream().allMatch(l -> "Late".equals(l.getStatus())));
    }

    // =========================================================================
    // 6. Tests para findAllByClientidAndStatus(Long clientid, String status)
    // =========================================================================

    @Test
    void testFindAllByClientidAndStatus_Client1Active_ReturnsOneLoan() {
        // Act
        List<LoanToolsEntity> loans = loanToolsRepository.findAllByClientidAndStatus(1L, "Active");

        // Assert
        assertEquals(1, loans.size());
        assertEquals(1L, loans.get(0).getToolid()); // Solo loan1
    }

    @Test
    void testFindAllByClientidAndStatus_Client2Returned_ReturnsOneLoan() {
        // Act
        List<LoanToolsEntity> loans = loanToolsRepository.findAllByClientidAndStatus(2L, "Returned");

        // Assert
        assertEquals(1, loans.size());
        assertEquals(4L, loans.get(0).getToolid()); // Solo loan4
    }

    @Test
    void testFindAllByClientidAndStatus_NoMatch_ReturnsEmptyList() {
        // Act
        List<LoanToolsEntity> loans = loanToolsRepository.findAllByClientidAndStatus(1L, "Returned");

        // Assert
        assertTrue(loans.isEmpty());
    }
}