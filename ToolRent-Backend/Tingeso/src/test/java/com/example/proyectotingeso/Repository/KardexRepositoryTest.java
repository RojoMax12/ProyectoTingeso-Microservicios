package com.example.proyectotingeso.Repository;

import static org.junit.jupiter.api.Assertions.*;

import com.example.proyectotingeso.Entity.KardexEntity;
import com.example.proyectotingeso.Entity.ToolEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@ActiveProfiles("test")
public class KardexRepositoryTest {

    @Autowired
    private KardexRepository kardexRepository;

    @Autowired
    private TestEntityManager entityManager;

    // IDs de estado simulados para la consulta @Query (Estado Prestado)
    private static final Long LOANED_STATE_ID = 2L;

    // ID de estado para ToolEntity
    private static final Long TOOL_AVAILABLE_STATE = 1L;

    // Fechas de prueba
    private final LocalDate dateToday = LocalDate.now();
    private final LocalDate dateYesterday = dateToday.minusDays(1);
    private final LocalDate dateLastWeek = dateToday.minusWeeks(1);

    // Entidades de prueba
    private ToolEntity hammer;
    private ToolEntity drill;
    private ToolEntity saw;

    @BeforeEach
    void setup() {
        // 1. Configurar y persistir ToolEntity
        // Constructor de ToolEntity: (id, name, category, replacement_cost (int), states (Long))
        hammer = new ToolEntity(null, "Hammer Pro", "Construction", 15, TOOL_AVAILABLE_STATE);
        drill = new ToolEntity(null, "Drill Master", "Electric", 80, TOOL_AVAILABLE_STATE);
        saw = new ToolEntity(null, "Saw Heavy Duty", "Construction", 120, TOOL_AVAILABLE_STATE);

        entityManager.persistAndFlush(hammer);
        entityManager.persistAndFlush(drill);
        entityManager.persistAndFlush(saw);

        // 2. Configurar y persistir KardexEntity
        // Constructor de KardexEntity: (id, StateToolsId, date, username, idtool, quantity)

        // Kardex 1: Martillo, Prestado (LOANED_STATE_ID=2), Ayer (Préstamo 1)
        entityManager.persistAndFlush(new KardexEntity(null, LOANED_STATE_ID, dateYesterday, "user1", hammer.getId(), 1));

        // Kardex 2: Taladro, Prestado (LOANED_STATE_ID=2), Ayer (Préstamo 1)
        entityManager.persistAndFlush(new KardexEntity(null, LOANED_STATE_ID, dateYesterday, "user2", drill.getId(), 1));

        // Kardex 3: Taladro, Prestado (LOANED_STATE_ID=2), Hoy (Préstamo 2 - Mismo Taladro)
        entityManager.persistAndFlush(new KardexEntity(null, LOANED_STATE_ID, dateToday, "user3", drill.getId(), 1));

        // Kardex 4: Taladro, De Vuelta (ID 1 - No Prestado), Hoy (No cuenta para getTopTools)
        entityManager.persistAndFlush(new KardexEntity(null, 1L, dateToday, "system", drill.getId(), 1));

        // Kardex 5: Martillo, Prestado (LOANED_STATE_ID=2), Hoy (Préstamo 2)
        entityManager.persistAndFlush(new KardexEntity(null, LOANED_STATE_ID, dateToday, "user4", hammer.getId(), 1));

        // Kardex 6: Sierra, Prestado (LOANED_STATE_ID=2), Hace una semana (Préstamo 1)
        entityManager.persistAndFlush(new KardexEntity(null, LOANED_STATE_ID, dateLastWeek, "user5", saw.getId(), 1));
    }

    // =========================================================================
    // 1. Tests para findById(Long id)
    // =========================================================================

    @Test
    void testFindById_KardexExists_ReturnsKardex() {
        // Arrange: Obtenemos el ID del primer Kardex creado
        KardexEntity firstKardex = entityManager.getEntityManager().createQuery("SELECT k FROM KardexEntity k ORDER BY k.id ASC", KardexEntity.class)
                .setMaxResults(1).getSingleResult();
        Long firstId = firstKardex.getId();

        // Act
        Optional<KardexEntity> found = kardexRepository.findById(firstId);

        // Assert
        assertTrue(found.isPresent());
        assertEquals(hammer.getId(), found.get().getIdtool());
        assertEquals(LOANED_STATE_ID, found.get().getStateToolsId());
    }

    // =========================================================================
    // 2. Tests para findAllByIdtool(Long idTool)
    // =========================================================================

    @Test
    void testFindAllByIdtool_ToolHasMultipleKardexEntries_ReturnsAll() {
        // Arrange: El Taladro (drill) tiene 3 entradas de préstamo/devolución
        Long drillId = drill.getId();

        // Act
        List<KardexEntity> entries = kardexRepository.findAllByIdtool(drillId);

        // Assert
        // K2, K3 (Prestado), K4 (Devuelto) = 3 entradas
        assertEquals(3, entries.size());
        assertTrue(entries.stream().allMatch(e -> e.getIdtool().equals(drillId)));
    }

    // =========================================================================
    // 3. Tests para findByDateBetweenOrderByDateDesc(LocalDate init, LocalDate fin)
    // =========================================================================

    @Test
    void testFindByDateBetween_ReturnsEntriesInDateRangeSortedDesc() {
        // Arrange
        // Rango: Ayer hasta Hoy
        LocalDate initDate = dateYesterday;
        LocalDate finalDate = dateToday;

        // Entradas esperadas: K1, K2, K3, K4, K5 (5 entradas)

        // Act
        List<KardexEntity> entries = kardexRepository.findByDateBetweenOrderByDateDesc(initDate, finalDate);

        // Assert
        assertEquals(5, entries.size(), "Debe retornar 5 entradas entre ayer y hoy.");

        // El primer reporte debe ser de hoy
        assertEquals(dateToday, entries.get(0).getDate());

        // El último reporte debe ser de ayer
        assertEquals(dateYesterday, entries.get(entries.size() - 1).getDate());
    }

    // =========================================================================
    // 4. Tests para getTopTools() -> Consulta personalizada (@Query)
    // =========================================================================

    @Test
    void testGetTopTools_ReturnsCorrectlySortedTopTools() {
        // Pre-Count: Martillo: 2, Taladro: 2, Sierra: 1

        // Damos un préstamo más al Martillo para romper el empate (Martillo: 3)
        // Constructor de KardexEntity: (id, StateToolsId, date, username, idtool, quantity)
        entityManager.persistAndFlush(new KardexEntity(null, LOANED_STATE_ID, dateToday, "user_extra", hammer.getId(), 1));

        // Nuevo Conteo: Martillo: 3, Taladro: 2, Sierra: 1

        // Act
        List<Object[]> topTools = kardexRepository.getTopTools();

        // Assert
        assertEquals(3, topTools.size(), "Debe retornar 3 herramientas únicas con préstamos.");

        // 1. Martillo (Hammer) con 3 préstamos
        assertEquals(hammer.getId(), (Long) topTools.get(0)[0], "ID de la herramienta con más préstamos debe ser Martillo.");
        assertEquals("Hammer Pro", (String) topTools.get(0)[1]);
        assertEquals(3L, (Long) topTools.get(0)[2]);

        // 2. Taladro (Drill) con 2 préstamos
        assertEquals(drill.getId(), (Long) topTools.get(1)[0]);
        assertEquals("Drill Master", (String) topTools.get(1)[1]);
        assertEquals(2L, (Long) topTools.get(1)[2]);

        // 3. Sierra (Saw) con 1 préstamo
        assertEquals(saw.getId(), (Long) topTools.get(2)[0]);
        assertEquals("Saw Heavy Duty", (String) topTools.get(2)[1]);
        assertEquals(1L, (Long) topTools.get(2)[2]);
    }

    @Test
    void testGetTopTools_NoLoanedEntriesFound_ReturnsEmptyList() {
        // Arrange: Limpiamos todos los datos y solo persistimos movimientos NO prestados (ej. StateToolsId = 1)
        entityManager.getEntityManager().createQuery("DELETE FROM KardexEntity").executeUpdate();

        // Creamos solo entradas de "Devuelto" (StateToolsId != 2)
        // Constructor de KardexEntity: (id, StateToolsId, date, username, idtool, quantity)
        entityManager.persistAndFlush(new KardexEntity(null, 1L, dateToday, "return_user", drill.getId(), 1));

        // Act
        List<Object[]> topTools = kardexRepository.getTopTools();

        // Assert
        assertTrue(topTools.isEmpty(), "Debe retornar una lista vacía si solo hay movimientos de devolución/no-préstamo.");
    }
}
