package com.example.proyectotingeso.Services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.proyectotingeso.Entity.StateToolsEntity;
import com.example.proyectotingeso.Entity.ToolEntity;
import com.example.proyectotingeso.Repository.RoleRepository;
import com.example.proyectotingeso.Repository.StateToolsRepository;
import com.example.proyectotingeso.Repository.ToolRepository;
import com.example.proyectotingeso.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ToolServicesTest {

    @Mock
    ToolRepository toolRepository;

    @Mock
    StateToolsRepository stateToolsRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @InjectMocks
    ToolServices toolServices;

    // Estados base para simulación
    private final StateToolsEntity availableState = new StateToolsEntity(1L, "Available");
    private final StateToolsEntity borrowedState = new StateToolsEntity(2L, "Borrowed");
    private final StateToolsEntity inRepairState = new StateToolsEntity(3L, "In repair");
    private final StateToolsEntity dischargedState = new StateToolsEntity(4L, "Discharged");

    // Herramienta base que se reinicia antes de cada test
    private ToolEntity baseTool;

    @BeforeEach
    public void setup() {
        // Inicializa los Mocks antes de cualquier test.
        MockitoAnnotations.openMocks(this);

        // Configuración común de estados (para todos los tests)
        when(stateToolsRepository.findByName("Available")).thenReturn(availableState);
        when(stateToolsRepository.findByName("Borrowed")).thenReturn(borrowedState);
        when(stateToolsRepository.findByName("In repair")).thenReturn(inRepairState);
        when(stateToolsRepository.findByName("Discharged")).thenReturn(dischargedState);

        // --- Inicialización para la mayoría de los tests (Mueve el contenido de initSaveTests aquí) ---

        // Herramienta válida de entrada (reconfigurada para cada test)
        baseTool = new ToolEntity(null, "Martillo", "Construcción", 50, null);

        // Simular el save devolviendo la misma entidad con un ID asignado
        when(toolRepository.save(any(ToolEntity.class))).thenAnswer(invocation -> {
            ToolEntity saved = invocation.getArgument(0);
            if (saved.getId() == null) saved.setId(1L);
            return saved;
        });

        // Por defecto, simular que no hay herramientas similares
        when(toolRepository.findFirstByNameOrderByName(anyString())).thenReturn(Optional.empty());
    }

    // =========================================================================
    // 1. Tests para save(ToolEntity toolEntity)
    // =========================================================================

    @Test
    void testSave_ValidTool_AssignsDefaultAvailableState() {
        // Arrange (baseTool ya es válido y tiene states=null, configurado en @BeforeEach)

        // Act
        ToolEntity savedTool = toolServices.save(baseTool);

        // Assert
        assertNotNull(savedTool.getId());
        assertEquals("Martillo", savedTool.getName());
        assertEquals(availableState.getId(), savedTool.getStates()); // Estado por defecto
        verify(toolRepository, times(1)).save(any(ToolEntity.class));
        verify(stateToolsRepository, times(1)).findByName("Available");
    }

    @Test
    void testSave_ValidTool_UsesProvidedState() {
        // Arrange
        baseTool.setStates(inRepairState.getId());

        // Act
        ToolEntity savedTool = toolServices.save(baseTool);

        // Assert
        assertEquals(inRepairState.getId(), savedTool.getStates());
        verify(toolRepository, times(1)).save(any(ToolEntity.class));
        verify(stateToolsRepository, never()).findByName("Available"); // No debe buscar el estado por defecto
    }

    @Test
    void testSave_AutocompletesCostAndCategory_WhenSimilarToolExists() {
        // Arrange
        ToolEntity similarTool = new ToolEntity(10L, "MARTILLO", "Heavy Duty", 150, availableState.getId());

        // La herramienta de entrada tiene costo bajo y categoría nula
        ToolEntity inputTool = new ToolEntity(null, "Martillo", null, 0, null);

        when(toolRepository.findFirstByNameOrderByName("Martillo")).thenReturn(Optional.of(similarTool));

        // Act
        ToolEntity savedTool = toolServices.save(inputTool);

        // Assert
        assertEquals(150, savedTool.getReplacement_cost(), "Debe autocompletar el costo.");
        assertEquals("Heavy Duty", savedTool.getCategory(), "Debe autocompletar la categoría.");
        assertEquals(availableState.getId(), savedTool.getStates());
        verify(toolRepository, times(1)).save(any(ToolEntity.class));
    }

    @Test
    void testSave_MissingName_ThrowsException() {
        // Arrange
        baseTool.setName(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> toolServices.save(baseTool));
        verify(toolRepository, never()).save(any(ToolEntity.class));
    }

    @Test
    void testSave_InvalidCost_ThrowsException() {
        // Arrange
        baseTool.setReplacement_cost(0);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> toolServices.save(baseTool));
        verify(toolRepository, never()).save(any(ToolEntity.class));
    }

    @Test
    void testSave_AvailableStateMissing_ThrowsIllegalStateException() {
        // Arrange
        // Resetea la simulación global SOLO para este caso
        when(stateToolsRepository.findByName("Available")).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> toolServices.save(baseTool));
        verify(toolRepository, never()).save(any(ToolEntity.class));
    }

    // --- Separador de sección ---

    // =========================================================================
    // 2. Tests para inventory(ToolEntity toolEntity)
    // =========================================================================

    @Test
    void testInventory_ToolsAvailable_ReturnsCorrectCount() {
        // Arrange
        String toolName = "Destornillador";
        ToolEntity inputTool = new ToolEntity(null, toolName, null, 0, null);

        List<ToolEntity> availableTools = Arrays.asList(
                new ToolEntity(1L, toolName, "Cat", 50, availableState.getId()),
                new ToolEntity(2L, toolName, "Cat", 50, availableState.getId())
        );

        when(toolRepository.findAllByNameAndStates(toolName, availableState.getId())).thenReturn(availableTools);

        // Act
        int count = toolServices.inventory(inputTool);

        // Assert
        assertEquals(2, count);
        verify(stateToolsRepository, times(1)).findByName("Available");
        verify(toolRepository, times(1)).findAllByNameAndStates(toolName, availableState.getId());
    }

    @Test
    void testInventory_NoToolsAvailable_ReturnsZero() {
        // Arrange
        String toolName = "Sierra";
        ToolEntity inputTool = new ToolEntity(null, toolName, null, 0, null);

        when(toolRepository.findAllByNameAndStates(toolName, availableState.getId())).thenReturn(Collections.emptyList());

        // Act
        int count = toolServices.inventory(inputTool);

        // Assert
        assertEquals(0, count);
    }

    @Test
    void testInventory_AvailableStateMissing_ReturnsZero() {
        // Arrange
        // Resetea la simulación global SOLO para este caso
        when(stateToolsRepository.findByName("Available")).thenReturn(null);
        ToolEntity inputTool = new ToolEntity(null, "Llave", null, 0, null);

        // Act
        int count = toolServices.inventory(inputTool);

        // Assert
        assertEquals(0, count);
        verify(toolRepository, never()).findAllByNameAndStates(anyString(), anyLong());
    }

    // --- Separador de sección ---

    // =========================================================================
    // 3. Tests para unsubscribeToolAdmin
    // =========================================================================

    @Test
    void testUnsubscribeToolAdmin_Success_SetsDischargedState() throws Exception {
        // Arrange
        Long toolId = 5L;
        ToolEntity tool = new ToolEntity(toolId, "Taladro", "Cat", 300, availableState.getId());
        when(toolRepository.findById(toolId)).thenReturn(Optional.of(tool));
        when(toolRepository.save(any(ToolEntity.class))).thenReturn(tool);

        // Act
        ToolEntity updatedTool = toolServices.unsubscribeToolAdmin(toolId);

        // Assert
        assertEquals(dischargedState.getId(), updatedTool.getStates());
        verify(stateToolsRepository, times(1)).findByName("Discharged");
        verify(toolRepository, times(1)).save(tool);
    }

    @Test
    void testUnsubscribeToolAdmin_ToolNotFound_ThrowsIllegalArgumentException() {
        // Arrange
        Long toolId = 99L;
        when(toolRepository.findById(toolId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> toolServices.unsubscribeToolAdmin(toolId));
        verify(toolRepository, never()).save(any(ToolEntity.class));
    }

    @Test
    void testUnsubscribeToolAdmin_DischargedStateMissing_ThrowsIllegalStateException() {
        // Arrange
        Long toolId = 5L;
        ToolEntity dummyTool = new ToolEntity(toolId, "Dummy", "Cat", 10, availableState.getId());
        when(toolRepository.findById(toolId)).thenReturn(Optional.of(dummyTool));

        // Resetea la simulación global SOLO para este caso
        when(stateToolsRepository.findByName("Discharged")).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> toolServices.unsubscribeToolAdmin(toolId));
        verify(toolRepository, never()).save(any(ToolEntity.class));
    }

    // --- Separador de sección ---

    // =========================================================================
    // 4. Tests para borrowedTool
    // =========================================================================

    @Test
    void testBorrowedTool_Success_SetsBorrowedState() throws Exception {
        // Arrange
        Long toolId = 6L;
        ToolEntity tool = new ToolEntity(toolId, "Nivel", "Medida", 100, availableState.getId());
        when(toolRepository.findById(toolId)).thenReturn(Optional.of(tool));
        when(toolRepository.save(any(ToolEntity.class))).thenReturn(tool);

        // Act
        ToolEntity updatedTool = toolServices.borrowedTool(toolId);

        // Assert
        assertEquals(borrowedState.getId(), updatedTool.getStates());
        verify(stateToolsRepository, times(1)).findByName("Borrowed");
        verify(toolRepository, times(1)).save(tool);
    }

    @Test
    void testBorrowedTool_ToolNotFound_ThrowsIllegalArgumentException() {
        // Arrange
        Long toolId = 99L;
        when(toolRepository.findById(toolId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> toolServices.borrowedTool(toolId));
        verify(toolRepository, never()).save(any(ToolEntity.class));
    }

    // --- Separador de sección ---

    // =========================================================================
    // 5. Tests para inrepair
    // =========================================================================

    @Test
    void testInRepair_ToolExists_SetsInRepairState() throws Exception {
        // Arrange
        Long toolId = 7L;
        ToolEntity tool = new ToolEntity(toolId, "Sierra", "Corte", 500, availableState.getId());

        when(toolRepository.findById(toolId)).thenReturn(Optional.of(tool));
        when(toolRepository.save(any(ToolEntity.class))).thenReturn(tool);

        // Act
        ToolEntity updatedTool = toolServices.inrepair(toolId);

        // Assert
        assertEquals(inRepairState.getId(), updatedTool.getStates());
        verify(stateToolsRepository, times(1)).findByName("In repair");
        verify(toolRepository, times(1)).save(tool);
    }

    // --- Separador de sección ---

    // =========================================================================
    // 6. Tests para updateTool (Lógica muy específica)
    // =========================================================================

    @Test
    void testUpdateTool_UpdatesNameCostCategory_KeepsExistingState() {
        // Arrange
        Long toolId = 8L;
        ToolEntity existingTool = new ToolEntity(toolId, "Old Name", "Old Cat", 10, borrowedState.getId());

        ToolEntity inputTool = new ToolEntity(toolId, "New Name", "New Category", 20, availableState.getId());

        when(toolRepository.findById(toolId)).thenReturn(Optional.of(existingTool));
        when(toolRepository.save(any(ToolEntity.class))).thenAnswer(invocation -> invocation.getArgument(0)); // Devuelve lo que guarda

        // Act
        ToolEntity updatedTool = toolServices.updateTool(inputTool);

        // Assert
        assertEquals("New Name", updatedTool.getName());
        assertEquals("New Category", updatedTool.getCategory());
        assertEquals(20, updatedTool.getReplacement_cost());
        assertEquals(borrowedState.getId(), updatedTool.getStates(), "El estado debe preservarse del original.");
        verify(toolRepository, times(1)).save(any(ToolEntity.class));
    }

    @Test
    void testUpdateTool_ToolNotFound_ThrowsNoSuchElementException() {
        // Arrange
        Long toolId = 99L;
        ToolEntity inputTool = new ToolEntity(toolId, "N", "C", 1, 1L);
        when(toolRepository.findById(toolId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(java.util.NoSuchElementException.class, () -> toolServices.updateTool(inputTool));
        verify(toolRepository, times(1)).findById(toolId);
        verify(toolRepository, never()).save(any(ToolEntity.class));
    }

    // --- Separador de sección ---

    // =========================================================================
    // 7. Tests para CRUD simple (getAlltool, getTool, deletetoolbyid)
    // =========================================================================

    @Test
    void testGetAllTool_ReturnsList() {
        // Arrange
        List<ToolEntity> tools = Arrays.asList(new ToolEntity(1L, "T1", "C1", 10, 1L));
        when(toolRepository.findAll()).thenReturn(tools);

        // Act
        List<ToolEntity> result = toolServices.getAlltool();

        // Assert
        assertEquals(1, result.size());
        verify(toolRepository, times(1)).findAll();
    }

    @Test
    void testGetTool_ReturnsTool() {
        // Arrange
        Long toolId = 1L;
        ToolEntity tool = new ToolEntity(toolId, "Test", "Cat", 10, 1L);
        when(toolRepository.findById(toolId)).thenReturn(Optional.of(tool));

        // Act
        ToolEntity result = toolServices.getTool(toolId);

        // Assert
        assertEquals(toolId, result.getId());
        verify(toolRepository, times(1)).findById(toolId);
    }

    @Test
    void testGetTool_NotFound_ThrowsNoSuchElementException() {
        // Arrange
        Long toolId = 99L;
        when(toolRepository.findById(toolId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(java.util.NoSuchElementException.class, () -> toolServices.getTool(toolId));
        verify(toolRepository, times(1)).findById(toolId);
    }

    @Test
    void testDeleteToolById_Success_ReturnsTrue() throws Exception {
        // Arrange
        Long toolId = 1L;
        doNothing().when(toolRepository).deleteById(toolId);

        // Act
        boolean result = toolServices.deletetoolbyid(toolId);

        // Assert
        assertTrue(result);
        verify(toolRepository, times(1)).deleteById(toolId);
    }

    @Test
    void testDeleteToolById_Failure_ThrowsException() {
        // Arrange
        Long toolId = 1L;
        String errorMessage = "Cannot delete due to foreign key constraint";
        doThrow(new RuntimeException(errorMessage)).when(toolRepository).deleteById(toolId);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            toolServices.deletetoolbyid(toolId);
        });

        assertTrue(exception.getMessage().contains(errorMessage));
        verify(toolRepository, times(1)).deleteById(toolId);
    }
}