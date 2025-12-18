package com.example.proyectotingeso.Repository;

import com.example.proyectotingeso.Entity.DataReportEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class DataReportRepositoryTest {

    @Autowired
    private DataReportRepository dataReportRepository;

    @Autowired
    private TestEntityManager entityManager;

    private final Long REPORT_ID_1 = 100L;
    private final Long REPORT_ID_2 = 200L;
    private final Long CLIENT_ID_1 = 1L;
    private final Long LOAN_ID_1 = 50L;
    private final Long TOOL_ID_1 = 5L;
    private final Long NUMBER_TIMES = 3L;

    // --- Método auxiliar para crear y persistir entidades ---
    private DataReportEntity createAndPersistReport(Long idReport, Long idClient, Long idLoanTool) {
        DataReportEntity report = new DataReportEntity();
        report.setIdreport(idReport);
        report.setIdClient(idClient);
        report.setIdLoanTool(idLoanTool);
        report.setIdTool(TOOL_ID_1);
        report.setNumber_of_times_borrowed(NUMBER_TIMES);
        return entityManager.persistAndFlush(report);
    }

    @BeforeEach
    void setup() {
        // Limpiar y asegurar que la DB contenga datos conocidos antes de cada test
        dataReportRepository.deleteAll();
        entityManager.clear();

        // Persistir datos de prueba
        createAndPersistReport(REPORT_ID_1, CLIENT_ID_1, LOAN_ID_1);
        createAndPersistReport(REPORT_ID_1, CLIENT_ID_1, 51L); // Segundo reporte del mismo cliente y ID_Reporte
        createAndPersistReport(REPORT_ID_2, 2L, 52L);           // Reporte diferente
    }

    // ------------------------------------------------------------------------------------------
    // 1. Test para findByidreport(Long idreport)
    // ------------------------------------------------------------------------------------------

    @Test
    void findByidreport_ShouldReturnTwoReports() {
        // Act
        List<DataReportEntity> foundReports = dataReportRepository.findByidreport(REPORT_ID_1);

        // Assert
        assertThat(foundReports).isNotEmpty();
        assertThat(foundReports).hasSize(2);
        assertThat(foundReports.get(0).getIdreport()).isEqualTo(REPORT_ID_1);
        assertThat(foundReports.get(1).getIdreport()).isEqualTo(REPORT_ID_1);
    }

    @Test
    void findByidreport_ShouldReturnEmptyListWhenNotFound() {
        // Act
        List<DataReportEntity> foundReports = dataReportRepository.findByidreport(999L);

        // Assert
        assertThat(foundReports).isEmpty();
    }

    // ------------------------------------------------------------------------------------------
    // 2. Test para existsByIdLoanTool(Long idLoanTool)
    // ------------------------------------------------------------------------------------------

    @Test
    void existsByIdLoanTool_ShouldReturnTrueWhenExists() {
        // Act
        boolean exists = dataReportRepository.existsByIdLoanTool(LOAN_ID_1);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByIdLoanTool_ShouldReturnFalseWhenNotExists() {
        // Act
        boolean exists = dataReportRepository.existsByIdLoanTool(999L);

        // Assert
        assertThat(exists).isFalse();
    }

    // ------------------------------------------------------------------------------------------
    // 3. Test para existsByIdClient(Long idClient)
    // ------------------------------------------------------------------------------------------

    @Test
    void existsByIdClient_ShouldReturnTrueWhenExists() {
        // Act
        boolean exists = dataReportRepository.existsByIdClient(CLIENT_ID_1);

        // Assert
        assertThat(exists).isTrue();
    }

    @Test
    void existsByIdClient_ShouldReturnFalseWhenNotExists() {
        // Act
        boolean exists = dataReportRepository.existsByIdClient(999L);

        // Assert
        assertThat(exists).isFalse();
    }
}