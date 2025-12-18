package com.example.proyectotingeso.Services;

import com.example.proyectotingeso.Entity.StateToolsEntity;
import com.example.proyectotingeso.Repository.StateToolsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

public class StateToolsServicesTest {

    @Mock
    private StateToolsRepository stateToolsRepository;

    @InjectMocks
    private StateToolsServices stateToolsServices;

    private StateToolsEntity availableState;
    private StateToolsEntity borrowedState;

    @BeforeEach
    void setUp() {
        availableState = new StateToolsEntity(1L, "Available");
        borrowedState = new StateToolsEntity(2L, "Borrowed");
    }
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateStateTools_whenStatesNotExist_thenCreateStates() {
        // Simula que no existen los estados "Available", "Borrowed", "In repair", "Discharged"
        when(stateToolsRepository.findByName("Available")).thenReturn(null);
        when(stateToolsRepository.findByName("Borrowed")).thenReturn(null);
        when(stateToolsRepository.findByName("In repair")).thenReturn(null);
        when(stateToolsRepository.findByName("Discharged")).thenReturn(null);

        // Llama al método createStateTools
        String result = stateToolsServices.createStateTools();

        // Verifica que los estados sean creados
        assertEquals("Estados de herramientas creado", result);

        // Verifica que los métodos de guardado se hayan llamado
        verify(stateToolsRepository, times(4)).save(any(StateToolsEntity.class));
    }

    @Test
    public void testCreateStateTools_whenStatesExist_thenDoNotCreate() {
        // Arrange: Entidades simuladas para los estados que *ya existen*
        // Nota: Necesitas estas variables disponibles en tu clase de test (ej. @BeforeEach)
        // Asumo que 'availableState' y 'borrowedState' ya están definidos.

        // 1. Simula que el estado "Available" existe
        when(stateToolsRepository.findByName("Available")).thenReturn(availableState);

        // 2. Simula que el estado "Borrowed" existe
        when(stateToolsRepository.findByName("Borrowed")).thenReturn(borrowedState);

        // 🛑 3. Simula que el estado "In repair" existe
        StateToolsEntity inRepairState = new StateToolsEntity(3L, "In repair");
        when(stateToolsRepository.findByName("In repair")).thenReturn(inRepairState);

        // 🛑 4. Simula que el estado "Discharged" existe
        StateToolsEntity dischargedState = new StateToolsEntity(4L, "Discharged");
        when(stateToolsRepository.findByName("Discharged")).thenReturn(dischargedState);

        // Llama al método createStateTools
        String result = stateToolsServices.createStateTools();

        // Assert: Verifica que el mensaje sea el de "ya iniciados"
        assertEquals("Estados de herramientas ya iniciados", result);

        // Assert: Verifica que no se haya intentado crear (guardar) ningún estado nuevo
        verify(stateToolsRepository, times(0)).save(any(StateToolsEntity.class));

        // Assert Opcional: Verifica que findByName se llamó 4 veces (una por cada estado verificado)
        verify(stateToolsRepository, times(1)).findByName("Available");
        verify(stateToolsRepository, times(1)).findByName("Borrowed");
        verify(stateToolsRepository, times(1)).findByName("In repair");
        verify(stateToolsRepository, times(1)).findByName("Discharged");
    }

    @Test
    public void testGetStateToolsEntityById_whenStateExists_thenReturnState() {
        // Simula la respuesta del repositorio
        when(stateToolsRepository.findById(1L)).thenReturn(Optional.of(availableState));

        // Llama al método getStateToolsEntityById
        StateToolsEntity result = stateToolsServices.getStateToolsEntityById(1L);

        // Verifica que el estado se haya encontrado correctamente
        assertNotNull(result);
        assertEquals("Available", result.getName());
    }

    @Test
    public void testGetStateToolsEntityById_whenStateDoesNotExist_thenReturnNull() {
        // Simula que no se encuentra el estado
        when(stateToolsRepository.findById(99L)).thenReturn(Optional.empty());

        // Llama al método getStateToolsEntityById
        StateToolsEntity result = stateToolsServices.getStateToolsEntityById(99L);

        // Verifica que no se haya encontrado el estado
        assertNull(result);
    }

    @Test
    public void testUpdateStateToolsEntity_whenStateExists_thenUpdateState() {
        // Simula la actualización de un estado
        StateToolsEntity updatedState = new StateToolsEntity(1L, "Updated State");
        when(stateToolsRepository.save(updatedState)).thenReturn(updatedState);

        // Llama al método updateStateToolsEntity
        StateToolsEntity result = stateToolsServices.updateStateToolsEntity(updatedState);

        // Verifica que el estado se haya actualizado correctamente
        assertNotNull(result);
        assertEquals("Updated State", result.getName());

        // Verifica que el repositorio haya sido llamado para guardar el estado actualizado
        verify(stateToolsRepository, times(1)).save(updatedState);
    }

    @Test
    public void testDeleteStateToolsById_whenStateExists_thenDeleteState() throws Exception {
        // Simula que el estado existe
        when(stateToolsRepository.findById(1L)).thenReturn(Optional.of(availableState));

        // Llama al método deleteStateToolsById
        boolean result = stateToolsServices.deleteStateToolsById(1L);

        // Verifica que el estado haya sido eliminado
        assertTrue(result);
        verify(stateToolsRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteStateToolsById_whenStateDoesNotExist_thenThrowException() {
        // Simula que el estado no existe
        when(stateToolsRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert: Verifica que se lance una excepción al intentar eliminar un estado que no existe
        assertThrows(Exception.class, () -> stateToolsServices.deleteStateToolsById(99L));
    }
}
