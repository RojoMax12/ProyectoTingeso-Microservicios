package com.example.proyectotingeso.Services;

import com.example.proyectotingeso.Entity.KardexEntity;
import com.example.proyectotingeso.Entity.ToolEntity;
import com.example.proyectotingeso.Repository.KardexRepository;
import com.example.proyectotingeso.Repository.ToolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class KardexServicesTest {

    @Mock
    private KardexRepository kardexRepository;

    @Mock
    private ToolRepository toolRepository;

    @InjectMocks
    private KardexServices kardexServices;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSave_whenValidKardexEntity_thenSaveSuccessfully() {
        // Arrange: Simula un objeto KardexEntity válido
        KardexEntity kardexEntity = new KardexEntity(1L, 1L, LocalDate.now(), "pepito", 10L, 1);

        // Simula la respuesta del repositorio
        when(kardexRepository.save(kardexEntity)).thenReturn(kardexEntity);

        // Act: Llama al método save del servicio
        KardexEntity result = kardexServices.save(kardexEntity);

        // Assert: Verifica que el resultado no sea nulo y que el ID coincida
        assertNotNull(result);
        assertEquals(kardexEntity.getId(), result.getId());

        // Verifica que el repositorio haya sido llamado correctamente
        verify(kardexRepository, times(1)).save(any(KardexEntity.class));
    }

    @Test
    public void testFindAll_whenKardexEntitiesExist_thenReturnAllEntities() {
        // Arrange: Simula objetos KardexEntity existentes
        KardexEntity kardex1 = new KardexEntity(1L, 1L, LocalDate.now(), "Pepito", 10L, 1);
        KardexEntity kardex2 = new KardexEntity(2L, 1L, LocalDate.now().minusDays(1), "Rodrigo", 20L, 1);

        // Simula la respuesta del repositorio
        when(kardexRepository.findAll()).thenReturn(Arrays.asList(kardex1, kardex2));

        // Act: Llama al método findAll del servicio
        List<KardexEntity> result = kardexServices.findAll();

        // Assert: Verifica que la lista no sea nula y tenga el tamaño correcto
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testUpdate_whenExistingKardexEntity_thenUpdateSuccessfully() {
        // Arrange: Simula un objeto KardexEntity existente
        KardexEntity kardexEntity = new KardexEntity(1L, 1L, LocalDate.now(), "Pepito", 10L, 1);

        // Simula la respuesta del repositorio
        when(kardexRepository.save(kardexEntity)).thenReturn(kardexEntity);

        // Act: Llama al método update del servicio
        KardexEntity result = kardexServices.Update(kardexEntity);

        // Assert: Verifica que el resultado no sea nulo y que el ID coincida
        assertNotNull(result);
        assertEquals(kardexEntity.getId(), result.getId());

        // Verifica que el repositorio haya sido llamado para guardar la entidad
        verify(kardexRepository, times(1)).save(any(KardexEntity.class));
    }

    @Test
    public void testDelete_whenValidId_thenDeleteSuccessfully() throws Exception {
        // Arrange: El id de un KardexEntity
        Long id = 1L;

        // Simula que no hay excepciones al borrar
        doNothing().when(kardexRepository).deleteById(id);

        // Act: Llama al método delete del servicio
        boolean result = kardexServices.delete(id);

        // Assert: Verifica que el resultado sea verdadero
        assertTrue(result);

        // Verifica que el repositorio haya sido llamado para eliminar la entidad
        verify(kardexRepository, times(1)).deleteById(id);
    }

    @Test
    public void testHistoryKardexTool_whenToolNameExists_thenReturnHistory() {
        // Arrange: Simula un ToolEntity y KardexEntity asociados
        ToolEntity tool = new ToolEntity(1L, "Hammer", "Tools", 50, 1L);
        KardexEntity kardex1 = new KardexEntity(1L, 1L, LocalDate.now(), "Pepito", 10L, 1);
        KardexEntity kardex2 = new KardexEntity(2L, 1L, LocalDate.now().minusDays(1), "active", 12L, 2);

        // Simula la respuesta de los repositorios
        when(toolRepository.findAllByName("Hammer")).thenReturn(Arrays.asList(tool));
        when(kardexRepository.findAllByIdtool(1L)).thenReturn(Arrays.asList(kardex1, kardex2));

        // Act: Llama al método HistoryKardexTool del servicio
        List<KardexEntity> history = kardexServices.HistoryKardexTool("Hammer");

        // Assert: Verifica que el resultado no sea nulo y tenga el tamaño correcto
        assertNotNull(history);
        assertEquals(2, history.size());
    }

    @Test
    public void testTopToolKardexTool_whenTopToolsExist_thenReturnTopTools() {
        // Arrange: Simula datos de herramientas populares
        Object[] toolData1 = new Object[]{1L, "Hammer", 5L};
        Object[] toolData2 = new Object[]{2L, "Wrench", 3L};

        // Simula la respuesta del repositorio
        when(kardexRepository.getTopTools()).thenReturn(Arrays.asList(toolData1, toolData2));

        // Act: Llama al método TopToolKardexTool del servicio
        List<Object[]> topTools = kardexServices.TopToolKardexTool();

        // Assert: Verifica que el resultado no sea nulo y tenga el tamaño correcto
        assertNotNull(topTools);
        assertEquals(2, topTools.size());
        assertEquals("Hammer", topTools.get(0)[1]);
        assertEquals(5L, topTools.get(0)[2]);
    }

    @Test
    public void testHistoryKardexDateInitandDateFin_whenDateRangeGiven_thenReturnHistory() {
        // Arrange: Simula el rango de fechas
        LocalDate start = LocalDate.of(2025, 1, 1);
        LocalDate end = LocalDate.of(2025, 12, 31);
        KardexEntity kardex1 = new KardexEntity(1L, 1L, LocalDate.now(), "Pepito", 10L, 1);

        // Simula la respuesta del repositorio
        when(kardexRepository.findByDateBetweenOrderByDateDesc(start, end)).thenReturn(Arrays.asList(kardex1));

        // Act: Llama al método HistoryKardexDateInitandDateFin del servicio
        List<KardexEntity> history = kardexServices.HistoryKardexDateInitandDateFin(start, end);

        // Assert: Verifica que el resultado no sea nulo y tenga el tamaño correcto
        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals(kardex1, history.get(0));
    }
}
