package com.example.proyectotingeso.Services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.proyectotingeso.Entity.ClientEntity;
import com.example.proyectotingeso.Entity.LoanToolsEntity;
import com.example.proyectotingeso.Entity.StateUsersEntity;
import com.example.proyectotingeso.Repository.ClientRepository;
import com.example.proyectotingeso.Repository.StateUsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ClientServicesTest {

    @Mock
    ClientRepository clientRepository;

    @Mock
    StateUsersRepository stateUsersRepository;

    @Mock
    LoanToolsServices loanToolsServices;

    @InjectMocks
    ClientServices clientServices;

    // Entidades simuladas
    private final StateUsersEntity activeState = new StateUsersEntity(1L, "Active");
    private ClientEntity newClient;
    private ClientEntity existingClient;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // Configuración por defecto para el estado "Active"
        when(stateUsersRepository.findByName("Active")).thenReturn(activeState);

        // Cliente base para tests de creación
        // Constructor basado en ClientEntity: (id, name, email, rut, phone, state)
        newClient = new ClientEntity(null, "Juan Perez", "juan@test.com", "12345678-9", "987654321", null);

        // Cliente existente para mocks
        existingClient = new ClientEntity(10L, "Ana Gomez", "ana@test.com", "98765432-1", "123456789", activeState.getId());

        // Simular que save devuelve la entidad con un ID asignado
        when(clientRepository.save(any(ClientEntity.class)))
                .thenAnswer(invocation -> {
                    ClientEntity client = invocation.getArgument(0);
                    if (client.getId() == null) client.setId(1L);
                    return client;
                });
    }

    // =========================================================================
    // 1. Tests para createClient(ClientEntity clientEntity)
    // =========================================================================

    @Nested
    class CreateClientTests {

        @Test
        void testCreateClient_Success_AssignsDefaultActiveState() {
            // Arrange
            // Simular que no existen duplicados
            when(clientRepository.findFirstByRut(anyString())).thenReturn(Optional.empty());
            when(clientRepository.findFirstByEmail(anyString())).thenReturn(Optional.empty());

            // Act
            ClientEntity createdClient = clientServices.createClient(newClient);

            // Assert
            assertNotNull(createdClient.getId());
            assertEquals(activeState.getId(), createdClient.getState(), "Debe asignar el estado 'Active'.");
            verify(clientRepository, times(1)).save(any(ClientEntity.class));
            verify(stateUsersRepository, times(1)).findByName("Active");
        }

        @Test
        void testCreateClient_Success_UsesProvidedState() {
            // Arrange
            Long restrictedStateId = 2L;
            newClient.setState(restrictedStateId); // Cliente ya trae un estado

            // Simular que no existen duplicados
            when(clientRepository.findFirstByRut(anyString())).thenReturn(Optional.empty());
            when(clientRepository.findFirstByEmail(anyString())).thenReturn(Optional.empty());

            // Act
            ClientEntity createdClient = clientServices.createClient(newClient);

            // Assert
            assertEquals(restrictedStateId, createdClient.getState(), "Debe usar el estado provisto.");
            // No debe buscar el estado por defecto
            verify(stateUsersRepository, never()).findByName(anyString());
            verify(clientRepository, times(1)).save(any(ClientEntity.class));
        }

        @Test
        void testCreateClient_DuplicateRut_ThrowsException() {
            // Arrange
            when(clientRepository.findFirstByRut(newClient.getRut())).thenReturn(Optional.of(existingClient));
            when(clientRepository.findFirstByEmail(newClient.getEmail())).thenReturn(Optional.empty());

            // Act & Assert
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                clientServices.createClient(newClient);
            });

            assertTrue(thrown.getMessage().contains("Ya existe un cliente con ese RUT"));
            verify(clientRepository, never()).save(any(ClientEntity.class));
        }

        @Test
        void testCreateClient_DuplicateEmail_ThrowsException() {
            // Arrange
            when(clientRepository.findFirstByRut(newClient.getRut())).thenReturn(Optional.empty());
            when(clientRepository.findFirstByEmail(newClient.getEmail())).thenReturn(Optional.of(existingClient));

            // Act & Assert
            IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
                clientServices.createClient(newClient);
            });

            assertTrue(thrown.getMessage().contains("Ya existe un cliente con ese email"));
            verify(clientRepository, never()).save(any(ClientEntity.class));
        }
    }

    // =========================================================================
    // 2. Tests para getAllClientLoanLate()
    // =========================================================================

    @Test
    void testGetAllClientLoanLate_ReturnsLateClients() {
        // Arrange

        // Fechas de ejemplo
        LocalDate initialDate = LocalDate.now().minusDays(10);
        LocalDate finalDate = LocalDate.now().minusDays(5); // Una fecha pasada (indica atraso)

        // 1. Configurar préstamos atrasados
        // Constructor de LoanToolsEntity:
        // (id, initiallenddate, finalreturndate, clientid, toolid, status, lateFee, rentalFee, damageFee, repositionFee)

        // Préstamo 1: Cliente 5
        LoanToolsEntity loan1 = new LoanToolsEntity(
                1L, initialDate, finalDate, 5L, 1L,
                "Late", 10.0, 50.0, 0.0, 0.0
        );

        // Préstamo 2: Cliente 6
        LoanToolsEntity loan2 = new LoanToolsEntity(
                2L, initialDate, finalDate, 6L, 2L,
                "Late", 20.0, 60.0, 0.0, 0.0
        );

        List<LoanToolsEntity> lateLoans = Arrays.asList(loan1, loan2);

        // Simular que el servicio de préstamos devuelve la lista de atrasados
        when(loanToolsServices.findallloanstoolstatusLate()).thenReturn(lateLoans);

        // 2. Clientes correspondientes a los IDs (Client ID 5 y 6)
        // Constructor: (id, name, email, rut, phone, state)
        ClientEntity client5 = new ClientEntity(5L, "Client A", "a@a.com", "C1", "123", 1L);
        ClientEntity client6 = new ClientEntity(6L, "Client B", "b@b.com", "C2", "456", 1L);
        List<ClientEntity> expectedClients = Arrays.asList(client5, client6);

        // Simular que el repositorio devuelve los clientes con IDs 5 y 6
        when(clientRepository.findAllById(Arrays.asList(5L, 6L))).thenReturn(expectedClients);

        // Act
        List<ClientEntity> actualClients = clientServices.getAllClientLoanLate();

        // Assert
        assertNotNull(actualClients);
        assertEquals(2, actualClients.size());
        assertEquals(5L, actualClients.get(0).getId());
        assertEquals(6L, actualClients.get(1).getId());

        verify(loanToolsServices, times(1)).findallloanstoolstatusLate();
        // Verificar que se llamó findAllById con los IDs correctos extraídos del préstamo
        verify(clientRepository, times(1)).findAllById(Arrays.asList(5L, 6L));
    }

    @Test
    void testGetAllClientLoanLate_NoLateLoans_ReturnsEmptyList() {
        // Arrange
        when(loanToolsServices.findallloanstoolstatusLate()).thenReturn(Collections.emptyList());

        // Act
        List<ClientEntity> actualClients = clientServices.getAllClientLoanLate();

        // Assert
        assertNotNull(actualClients);
        assertTrue(actualClients.isEmpty());

        verify(loanToolsServices, times(1)).findallloanstoolstatusLate();
        // Cuando la lista de IDs está vacía, findAllById no debe ser llamado.
        verify(clientRepository, never()).findAllById(anyList());
    }

    // =========================================================================
    // 3. Tests para CRUD básico
    // =========================================================================

    @Test
    void testGetAllClients_ReturnsAllClients() {
        // Arrange
        // Crear una instancia de ClientEntity válida con el constructor NoArgsConstructor y seters.
        ClientEntity dummyClient = new ClientEntity();
        dummyClient.setId(11L);
        dummyClient.setName("Dummy");

        List<ClientEntity> clients = Arrays.asList(existingClient, dummyClient);
        when(clientRepository.findAll()).thenReturn(clients);

        // Act
        List<ClientEntity> result = clientServices.getAllClients();

        // Assert
        assertEquals(2, result.size());
        verify(clientRepository, times(1)).findAll();
    }

    @Test
    void testGetClientByRut_ClientFound_ReturnsClient() {
        // Arrange
        String rut = "11223344-5";
        // Constructor: (id, name, email, rut, phone, state)
        ClientEntity client = new ClientEntity(2L, "Test User", "t@u.com", rut, "555", 1L);
        when(clientRepository.findByRut(rut)).thenReturn(client);

        // Act
        ClientEntity result = clientServices.getClientByRut(rut);

        // Assert
        assertNotNull(result);
        assertEquals(rut, result.getRut());
        verify(clientRepository, times(1)).findByRut(rut);
    }

    @Test
    void testGetClientById_ClientFound_ReturnsClient() {
        // Arrange
        Long id = 10L;
        when(clientRepository.findById(id)).thenReturn(Optional.of(existingClient));

        // Act
        ClientEntity result = clientServices.getClientById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(clientRepository, times(1)).findById(id);
    }

    @Test
    void testUpdateClient_Success_ReturnsUpdatedClient() {
        // Arrange
        // Constructor: (id, name, email, rut, phone, state)
        ClientEntity clientToUpdate = new ClientEntity(10L, "Ana Updated", "ana_new@test.com", "98765432-1", "999888777", activeState.getId());
        when(clientRepository.save(clientToUpdate)).thenReturn(clientToUpdate);

        // Act
        ClientEntity result = clientServices.updateClient(clientToUpdate);

        // Assert
        assertEquals("Ana Updated", result.getName(), "El nombre debe estar actualizado.");
        assertEquals("ana_new@test.com", result.getEmail());
        verify(clientRepository, times(1)).save(clientToUpdate);
    }

    @Test
    void testDeleteClient_Success_ReturnsTrue() throws Exception {
        // Arrange
        Long id = 10L;
        doNothing().when(clientRepository).deleteById(id);

        // Act
        boolean result = clientServices.deleteClient(id);

        // Assert
        assertTrue(result);
        verify(clientRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteClient_Failure_ThrowsException() {
        // Arrange
        Long id = 10L;
        String errorMessage = "Foreign key constraint violation";
        doThrow(new RuntimeException(errorMessage)).when(clientRepository).deleteById(id);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            clientServices.deleteClient(id);
        });

        assertTrue(exception.getMessage().contains(errorMessage));
        verify(clientRepository, times(1)).deleteById(id);
    }
}