package com.example.proyectotingeso.Services;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.proyectotingeso.Entity.RoleEntity;
import com.example.proyectotingeso.Repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RoleServicesTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServices roleServices;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // =========================================================================
    // 1. Tests para createRole
    // =========================================================================

    @Nested
    class CreateRoleTests {

        @Test
        public void testCreateRole_NoRolesExist_CreatesAllRoles() {
            // Given: Simular que ningún rol existe (findByName devuelve null)
            when(roleRepository.findByName(anyString())).thenReturn(null);

            // Simular el comportamiento del save
            when(roleRepository.save(any(RoleEntity.class))).thenAnswer(invocation -> {
                RoleEntity role = invocation.getArgument(0);
                role.setId(1L); // Asignar un ID simulado
                return role;
            });

            // When
            String result = roleServices.createRole();

            // Then
            assertEquals("Roles creados correctamente", result, "Debe indicar que los roles fueron creados.");

            // Verificar que findByName fue llamado 3 veces (Employer, Admin, Client)
            verify(roleRepository, times(1)).findByName("Employer");
            verify(roleRepository, times(1)).findByName("Admin");
            verify(roleRepository, times(1)).findByName("Client");

            // Verificar que save fue llamado 3 veces (uno por cada rol nuevo)
            verify(roleRepository, times(3)).save(any(RoleEntity.class));
        }

        @Test
        public void testCreateRole_AllRolesExist_ReturnsAlreadyInitialized() {
            // Given: Simular que todos los roles ya existen (findByName devuelve una entidad)
            when(roleRepository.findByName("Employer")).thenReturn(new RoleEntity(1L, "Employer"));
            when(roleRepository.findByName("Admin")).thenReturn(new RoleEntity(2L, "Admin"));
            when(roleRepository.findByName("Client")).thenReturn(new RoleEntity(3L, "Client"));

            // When
            String result = roleServices.createRole();

            // Then
            assertEquals("Roles ya inicializados", result, "Debe indicar que los roles ya estaban inicializados.");

            // Verificar que save nunca fue llamado
            verify(roleRepository, never()).save(any(RoleEntity.class));
        }

        @Test
        public void testCreateRole_OnlyAdminIsMissing_CreatesOnlyAdmin() {
            // Given: Simular que Employer y Client existen, pero Admin no
            when(roleRepository.findByName("Employer")).thenReturn(new RoleEntity(1L, "Employer"));
            when(roleRepository.findByName("Admin")).thenReturn(null); // Faltante
            when(roleRepository.findByName("Client")).thenReturn(new RoleEntity(3L, "Client"));

            // When
            String result = roleServices.createRole();

            // Then
            assertEquals("Roles creados correctamente", result, "Debe indicar que los roles fueron creados.");

            // Verificar que save fue llamado exactamente 1 vez (solo para Admin)
            verify(roleRepository, times(1)).save(argThat(role -> "Admin".equals(role.getName())));
        }
    }

    // =========================================================================
    // 2. Tests para getAllRoles
    // =========================================================================

    @Test
    public void testGetAllRoles_ReturnsListOfRoles() {
        // Given
        List<RoleEntity> expectedRoles = Arrays.asList(
                new RoleEntity(1L, "Employer"),
                new RoleEntity(2L, "Admin")
        );
        when(roleRepository.findAll()).thenReturn(expectedRoles);

        // When
        List<RoleEntity> result = roleServices.getAllRoles();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Employer", result.get(0).getName());
        verify(roleRepository, times(1)).findAll();
    }

    // =========================================================================
    // 3. Tests para getRoleById
    // =========================================================================

    @Test
    public void testGetRoleById_RoleExists_ReturnsOptionalRole() {
        // Given
        Long roleId = 5L;
        RoleEntity expectedRole = new RoleEntity(roleId, "CustomRole");
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(expectedRole));

        // When
        Optional<RoleEntity> result = roleServices.getRoleById(roleId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(roleId, result.get().getId());
        verify(roleRepository, times(1)).findById(roleId);
    }

    @Test
    public void testGetRoleById_RoleNotFound_ReturnsEmptyOptional() {
        // Given
        Long roleId = 99L;
        when(roleRepository.findById(roleId)).thenReturn(Optional.empty());

        // When
        Optional<RoleEntity> result = roleServices.getRoleById(roleId);

        // Then
        assertFalse(result.isPresent());
        verify(roleRepository, times(1)).findById(roleId);
    }

    // =========================================================================
    // 4. Tests para deleteRole
    // =========================================================================

    @Test
    public void testDeleteRole_Success_ReturnsTrue() throws Exception {
        // Given
        Long roleId = 10L;
        // Simular que la operación de eliminación es exitosa (void method)
        doNothing().when(roleRepository).deleteById(roleId);

        // When
        boolean result = roleServices.deleteRole(roleId);

        // Then
        assertTrue(result);
        verify(roleRepository, times(1)).deleteById(roleId);
    }

    @Test
    public void testDeleteRole_Failure_ThrowsException() {
        // Given
        Long roleId = 10L;
        String errorMessage = "Database constraint violation";
        // Simular que deleteById lanza una excepción
        doThrow(new RuntimeException(errorMessage)).when(roleRepository).deleteById(roleId);

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            roleServices.deleteRole(roleId);
        });

        assertTrue(exception.getMessage().contains(errorMessage));
        verify(roleRepository, times(1)).deleteById(roleId);
    }

    // =========================================================================
    // 5. Tests para updateRole
    // =========================================================================

    @Test
    public void testUpdateRole_Success_ReturnsUpdatedRole() {
        // Given
        RoleEntity roleToUpdate = new RoleEntity(1L, "UpdatedName");
        // Simular que save devuelve la entidad pasada
        when(roleRepository.save(roleToUpdate)).thenReturn(roleToUpdate);

        // When
        RoleEntity result = roleServices.updateRole(roleToUpdate);

        // Then
        assertNotNull(result);
        assertEquals("UpdatedName", result.getName());
        verify(roleRepository, times(1)).save(roleToUpdate);
    }
}