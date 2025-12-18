package com.example.proyectotingeso.Repository;

import static org.junit.jupiter.api.Assertions.*;

import com.example.proyectotingeso.Entity.ReportEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

@DataJpaTest
@ActiveProfiles("test")
public class ReportRepositoryTest {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private TestEntityManager entityManager;

    // Fechas de prueba
    private final LocalDate dateToday = LocalDate.of(2025, 11, 30);
    private final LocalDate dateYesterday = dateToday.minusDays(1);
    private final LocalDate dateLastWeek = dateToday.minusWeeks(1);
    private final LocalDate dateNextWeek = dateToday.plusWeeks(1);

    // Entidades de prueba
    private ReportEntity reportA;
    private ReportEntity reportB;
    private ReportEntity reportC;
    private ReportEntity reportD;
    private ReportEntity reportE;

    @BeforeEach
    void setup() {
        // Constructor simulado: (id, name, description, date)

        // Report A: "Sales Summary", Hoy
        reportA = new ReportEntity(null, "Sales Summary", dateToday);
        entityManager.persistAndFlush(reportA);

        // Report B: "Inventory Update", Ayer
        reportB = new ReportEntity(null, "Inventory Update",  dateYesterday);
        entityManager.persistAndFlush(reportB);

        // Report C: "Sales Summary", Hace una semana (mismo nombre que A)
        reportC = new ReportEntity(null, "Sales Summary", dateLastWeek);
        entityManager.persistAndFlush(reportC);

        // Report D: "Financial Audit", Mañana (fuera del rango principal)
        reportD = new ReportEntity(null, "Financial Audit", dateNextWeek);
        entityManager.persistAndFlush(reportD);

        // Report E: "Inventory Update", Hoy (mismo nombre que B)
        reportE = new ReportEntity(null, "Inventory Update", dateToday);
        entityManager.persistAndFlush(reportE);
    }

    // --- Métodos del Repository ---

    // 1. findByDateBetweenOrderByDateDesc(LocalDate init, LocalDate fin)

    @Test
    void testFindByDateBetween_ReturnsReportsInDateRangeSortedDesc() {
        // Arrange
        // Rango: Desde hace 10 días hasta hoy (incluye C, B, A, E)
        LocalDate initDate = dateToday.minusDays(10);
        LocalDate finalDate = dateToday;

        // Act
        List<ReportEntity> reports = reportRepository.findByDateBetweenOrderByDateDesc(initDate, finalDate);

        // Assert
        assertEquals(4, reports.size(), "Debe retornar 4 reportes dentro del rango.");

        // Verificación del orden descendente (el más reciente primero)
        // El primer reporte debe ser de hoy
        assertEquals(dateToday, reports.get(0).getDate());

        // El último reporte debe ser el más antiguo en el rango (C)
        assertEquals(dateLastWeek, reports.get(reports.size() - 1).getDate());
    }

    @Test
    void testFindByDateBetween_ReturnsEmptyListWhenNoReportsInDateRange() {
        // Arrange
        // Rango: Un mes en el futuro
        LocalDate initDate = dateToday.plusMonths(1);
        LocalDate finalDate = dateToday.plusMonths(1).plusDays(5);

        // Act
        List<ReportEntity> reports = reportRepository.findByDateBetweenOrderByDateDesc(initDate, finalDate);

        // Assert
        assertTrue(reports.isEmpty(), "No debe retornar reportes para un rango futuro.");
    }

    // 2. findByName(String name)

    @Test
    void testFindByName_MultipleReportsFound_ReturnsAllMatchingReports() {
        // Arrange
        String targetName = "Sales Summary"; // Reportes A y C

        // Act
        List<ReportEntity> reports = reportRepository.findByName(targetName);

        // Assert
        assertEquals(2, reports.size(), "Debe retornar los dos reportes con el mismo nombre.");
        assertTrue(reports.stream().allMatch(r -> targetName.equals(r.getName())));
    }

    @Test
    void testFindByName_ReportNotFound_ReturnsEmptyList() {
        // Arrange
        String targetName = "HR Analysis";

        // Act
        List<ReportEntity> reports = reportRepository.findByName(targetName);

        // Assert
        assertTrue(reports.isEmpty(), "No debe retornar reportes para un nombre que no existe.");
    }

}