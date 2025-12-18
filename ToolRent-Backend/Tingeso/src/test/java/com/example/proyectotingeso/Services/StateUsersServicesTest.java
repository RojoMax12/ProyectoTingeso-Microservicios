package com.example.proyectotingeso.Services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.proyectotingeso.Entity.StateUsersEntity;
import com.example.proyectotingeso.Repository.StateUsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class StateUsersServicesTest {

    @Mock
    private StateUsersRepository stateUsersRepository;

    @InjectMocks
    private StateUsersServices stateUsersServices;

    // Entidades simuladas para los estados conocidos
    private final StateUsersEntity activeState = new StateUsersEntity(1L, "Active");
    private final StateUsersEntity restrictedState = new StateUsersEntity(2L, "Restricted");

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================================================================
    // 1. Tests para CreateStateUsers()
    // =========================================================================

    @Test
    void testCreateStateUsers_NoStatesExist_CreatesBothStates() {
        // Arrange: Simular que ningún estado existe (findByName devuelve null)
        when(stateUsersRepository.findByName(anyString())).thenReturn(null);

        // Simular el save devolviendo la entidad (simulando que se asigna un ID)
        when(stateUsersRepository.save(any(StateUsersEntity.class)))
                .thenAnswer(invocation -> {
                    StateUsersEntity state = invocation.getArgument(0);
                    if ("Active".equals(state.getName())) state.setId(1L);
                    if ("Restricted".equals(state.getName())) state.setId(2L);
                    return state;
                });

        // Act
        String result = stateUsersServices.CreateStateUsers();

        // Assert
        assertEquals("Estados creados con exito", result, "Debe indicar éxito en la creación.");

        // Verificar que findByName fue llamado para ambos
        verify(stateUsersRepository, times(1)).findByName("Active");
        verify(stateUsersRepository, times(1)).findByName("Restricted");

        // Verificar que save fue llamado dos veces
        verify(stateUsersRepository, times(2)).save(any(StateUsersEntity.class));
    }

    @Test
    void testCreateStateUsers_OnlyOneStateExists_CreatesMissingState() {
        // Arrange: Simular que 'Active' existe, pero 'Restricted' no
        when(stateUsersRepository.findByName("Active")).thenReturn(activeState);
        when(stateUsersRepository.findByName("Restricted")).thenReturn(null);

        // Act
        String result = stateUsersServices.CreateStateUsers();

        // Assert
        assertEquals("Estados creados con exito", result, "Debe indicar éxito en la creación.");

        // Verificar que save fue llamado una vez (solo para 'Restricted')
        verify(stateUsersRepository, times(1)).save(argThat(state -> "Restricted".equals(state.getName())));
    }

    @Test
    void testCreateStateUsers_AllStatesExist_ReturnsAlreadyCreatedMessage() {
        // Arrange: Simular que ambos estados existen
        when(stateUsersRepository.findByName("Active")).thenReturn(activeState);
        when(stateUsersRepository.findByName("Restricted")).thenReturn(restrictedState);

        // Act
        String result = stateUsersServices.CreateStateUsers();

        // Assert
        // NOTA: El mensaje esperado es "Estados creados" (que es confuso, pero es el que está en el servicio)
        assertEquals("Estados creados", result, "Debe indicar que los estados ya estaban creados.");

        // Verificar que save nunca fue llamado
        verify(stateUsersRepository, never()).save(any(StateUsersEntity.class));
    }


    // =========================================================================
    // 2. Tests para getStateUsersById(Long id)
    // =========================================================================

    @Test
    void testGetStateUsersById_StateExists_ReturnsState() {
        // Arrange
        Long id = 1L;
        when(stateUsersRepository.findById(id)).thenReturn(Optional.of(activeState));

        // Act
        StateUsersEntity result = stateUsersServices.getStateUsersById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Active", result.getName());
    }

    @Test
    void testGetStateUsersById_StateNotFound_ReturnsNull() {
        // Arrange
        Long id = 99L;
        when(stateUsersRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        StateUsersEntity result = stateUsersServices.getStateUsersById(id);

        // Assert
        assertNull(result);
    }

    // =========================================================================
    // 3. Tests para getAllStateUsers()
    // =========================================================================

    @Test
    void testGetAllStateUsers_ReturnsListOfStates() {
        // Arrange
        List<StateUsersEntity> expectedList = Arrays.asList(activeState, restrictedState);
        when(stateUsersRepository.findAll()).thenReturn(expectedList);

        // Act
        List<StateUsersEntity> result = stateUsersServices.getAllStateUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Active", result.get(0).getName());
        verify(stateUsersRepository, times(1)).findAll();
    }

    // =========================================================================
    // 4. Tests para updateStateUsers(StateUsersEntity)
    // =========================================================================

    @Test
    void testUpdateStateUsers_Success_ReturnsUpdatedState() {
        // Arrange
        StateUsersEntity stateToUpdate = new StateUsersEntity(1L, "Suspended");
        when(stateUsersRepository.save(stateToUpdate)).thenReturn(stateToUpdate);

        // Act
        StateUsersEntity result = stateUsersServices.updateStateUsers(stateToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals("Suspended", result.getName());
        verify(stateUsersRepository, times(1)).save(stateToUpdate);
    }

    // =========================================================================
    // 5. Tests para deleteStateUsersById(Long id)
    // =========================================================================

    @Test
    void testDeleteStateUsersById_Success_ReturnsTrue() throws Exception {
        // Arrange
        Long id = 1L;
        // Simular que la operación de eliminación es exitosa
        doNothing().when(stateUsersRepository).deleteById(id);

        // Act
        boolean result = stateUsersServices.deleteStateUsersById(id);

        // Assert
        assertTrue(result);
        verify(stateUsersRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteStateUsersById_Failure_ThrowsException() {
        // Arrange
        Long id = 1L;
        String errorMessage = "Cannot delete due to foreign key constraint";
        // Simular que deleteById lanza una excepción (ej. por FK)
        doThrow(new RuntimeException(errorMessage)).when(stateUsersRepository).deleteById(id);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            stateUsersServices.deleteStateUsersById(id);
        });

        assertTrue(exception.getMessage().contains(errorMessage));
        verify(stateUsersRepository, times(1)).deleteById(id);
    }
}