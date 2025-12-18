package com.example.proyectotingeso.Services;

import com.example.proyectotingeso.Entity.RoleEntity;
import com.example.proyectotingeso.Entity.StateUsersEntity;
import com.example.proyectotingeso.Entity.ToolEntity;
import com.example.proyectotingeso.Entity.UserEntity;
import com.example.proyectotingeso.Repository.RoleRepository;
import com.example.proyectotingeso.Repository.StateUsersRepository;
import com.example.proyectotingeso.Repository.ToolRepository;
import com.example.proyectotingeso.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServicesTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private StateUsersRepository stateUsersRepository;

    @Mock
    private ToolRepository toolRepository;

    @InjectMocks
    private UserServices userServices;

    // Entidades base para simulación
    private final RoleEntity employerRole = new RoleEntity(1L, "Employer");
    private final RoleEntity adminRole = new RoleEntity(2L, "Admin");
    private final StateUsersEntity activeState = new StateUsersEntity(1L, "Active");

    // Datos comunes para los tests de login y save
    private final String VALID_RUT = "12345678-9";
    private final String VALID_PASSWORD = "password";
    private final Long USER_ROLE_ID = 1L;
    private final Long USER_STATE_ID = 1L;


    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(roleRepository.findByName("Employer")).thenReturn(employerRole);
        when(stateUsersRepository.findByName("Active")).thenReturn(activeState);
    }

    // =========================================================================
    // 1. Tests para login
    // =========================================================================

    @Test
    public void testLogin_whenUserExistsAndCredentialsAreCorrect_thenReturnTrue() {
        // Given
        // UserEntity(id, name, email, password, phone, state, rut, role)
        UserEntity loginAttempt = new UserEntity(null, "Test User", "test@test.com", VALID_PASSWORD, "123", USER_STATE_ID, VALID_RUT, USER_ROLE_ID);
        UserEntity existingUser = new UserEntity(1L, "Test User", "test@test.com", VALID_PASSWORD, "123", USER_STATE_ID, VALID_RUT, USER_ROLE_ID);
        when(userRepository.findByRut(VALID_RUT)).thenReturn(existingUser);

        // When
        boolean result = userServices.login(loginAttempt);

        // Then
        assertTrue(result);
        verify(userRepository, times(1)).findByRut(VALID_RUT);
    }

    @Test
    public void testLogin_whenUserDoesNotExist_thenReturnFalse() {
        // Given
        String nonExistentRut = "99999999-9";
        // UserEntity(id, name, email, password, phone, state, rut, role)
        UserEntity loginAttempt = new UserEntity(null, "Fake User", "fake@test.com", VALID_PASSWORD, "000", USER_STATE_ID, nonExistentRut, USER_ROLE_ID);
        when(userRepository.findByRut(nonExistentRut)).thenReturn(null);

        // When
        boolean result = userServices.login(loginAttempt);

        // Then
        assertFalse(result);
        verify(userRepository, times(1)).findByRut(nonExistentRut);
    }

    @Test
    public void testLogin_whenIncorrectPassword_thenReturnFalse() {
        // Given
        String wrongPassword = "wrong_password";
        // UserEntity(id, name, email, password, phone, state, rut, role)
        UserEntity loginAttempt = new UserEntity(null, "Test User", "test@test.com", wrongPassword, "123", USER_STATE_ID, VALID_RUT, USER_ROLE_ID);
        UserEntity existingUser = new UserEntity(1L, "Test User", "test@test.com", VALID_PASSWORD, "123", USER_STATE_ID, VALID_RUT, USER_ROLE_ID);
        when(userRepository.findByRut(VALID_RUT)).thenReturn(existingUser);

        // When
        boolean result = userServices.login(loginAttempt);

        // Then
        assertFalse(result);
    }

    @Test
    public void testLogin_whenRutMismatch_thenReturnFalse() {
        // Given
        String mismatchedRut = "99999999-9";
        // UserEntity(id, name, email, password, phone, state, rut, role)
        UserEntity loginAttempt = new UserEntity(null, "Test User", "test@test.com", VALID_PASSWORD, "123", USER_STATE_ID, VALID_RUT, USER_ROLE_ID);
        // Simular que findByRut("1234...") devuelve un usuario con un RUT diferente ("9999...")
        UserEntity existingUser = new UserEntity(1L, "Other User", "other@test.com", VALID_PASSWORD, "456", USER_STATE_ID, mismatchedRut, USER_ROLE_ID);
        when(userRepository.findByRut(VALID_RUT)).thenReturn(existingUser);

        // When
        boolean result = userServices.login(loginAttempt);

        // Then
        assertFalse(result, "Debe fallar si los RUT no coinciden, incluso si la contraseña es correcta.");
    }


    // =========================================================================
    // 2. Tests para saveUser
    // =========================================================================

    @Test
    public void testSaveUser_whenRoleIsNull_thenAssignDefaultRoleAndState() {
        // Given
        // UserEntity(id, name, email, password, phone, state=null, rut, role=null)
        UserEntity user = new UserEntity(null, "New", "new@test.com", "pass", "123", null, "111", null);

        // Simulación: Guardar retorna el mismo objeto (con el rol asignado por el servicio)
        when(userRepository.save(ArgumentMatchers.any())).thenAnswer(i -> (UserEntity) i.getArguments()[0]);

        // When
        UserEntity savedUser = userServices.saveUser(user);

        // Then
        assertEquals(employerRole.getId(), savedUser.getRole(), "Debe asignarse el ID de rol por defecto.");
        assertEquals(activeState.getId(), savedUser.getState(), "Debe asignarse el ID de estado por defecto.");
        verify(roleRepository, times(1)).findByName("Employer");
        verify(stateUsersRepository, times(1)).findByName("Active");
        verify(userRepository, times(1)).save(ArgumentMatchers.any());
    }

    @Test
    public void testSaveUser_whenValidRoleIsProvided_thenSaveUserWithProvidedRole() {
        // Given
        // UserEntity(id, name, email, password, phone, state=null, rut, role=2L)
        UserEntity user = new UserEntity(null, "Admin", "admin@test.com", "pass", "123", null, "222", adminRole.getId());

        when(roleRepository.findById(adminRole.getId())).thenReturn(Optional.of(adminRole));
        when(userRepository.save(ArgumentMatchers.any())).thenAnswer(i -> (UserEntity) i.getArguments()[0]);

        // When
        UserEntity savedUser = userServices.saveUser(user);

        // Then
        assertEquals(adminRole.getId(), savedUser.getRole(), "Debe usarse el rol provisto.");
        assertNull(savedUser.getState(), "El estado debe permanecer nulo si se provee un rol y el estado no se define.");
        verify(roleRepository, times(1)).findById(adminRole.getId());
        verify(userRepository, times(1)).save(ArgumentMatchers.any());
        verify(roleRepository, never()).findByName(anyString());
    }

    @Test
    public void testSaveUser_whenInvalidRoleIsProvided_thenThrowIllegalArgumentException() {
        // Given
        Long invalidRoleId = 99L;
        // UserEntity(id, name, email, password, phone, state=null, rut, role=99L)
        UserEntity user = new UserEntity(null, "Bad", "bad@test.com", "pass", "123", null, "333", invalidRoleId);
        when(roleRepository.findById(invalidRoleId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userServices.saveUser(user);
        });
        assertTrue(exception.getMessage().contains("El rol con id 99 no existe"));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    public void testSaveUser_whenDefaultRoleIsMissing_thenThrowIllegalStateException() {
        // Given
        // UserEntity(id, name, email, password, phone, state=null, rut, role=null)
        UserEntity user = new UserEntity(null, "Test", "test@test.com", "pass", "123", null, "444", null);
        when(roleRepository.findByName("Employer")).thenReturn(null); // Simular que el rol por defecto no existe

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            userServices.saveUser(user);
        });
        assertTrue(exception.getMessage().contains("El rol por defecto 'Employer' no está inicializado"));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    // =========================================================================
    // 3. Tests para Changereplacement_costTool
    // =========================================================================

    @Test
    public void testChangereplacement_costTool_whenAdmin_thenUpdateCostAndSave() {
        // Given
        Long adminId = 1L;
        String toolName = "Martillo";
        int newCost = 500;

        // UserEntity(id, name, email, password, phone, state, rut, role)
        UserEntity adminUser = new UserEntity(adminId, "Admin", "a@a.com", "pass", "111", USER_STATE_ID, "123", adminRole.getId());

        // NOTA: Creamos las entidades ToolEntity directamente en la simulación
        ToolEntity tool1 = new ToolEntity(10L, toolName, "Cat", 100, 1L);
        ToolEntity tool2 = new ToolEntity(11L, toolName, "Cat", 100, 1L);
        List<ToolEntity> foundTools = Arrays.asList(tool1, tool2);

        when(userRepository.findById(adminId)).thenReturn(Optional.of(adminUser));
        when(roleRepository.findById(adminRole.getId())).thenReturn(Optional.of(adminRole));
        when(toolRepository.findAllByName(toolName)).thenReturn(foundTools);

        // 🛑 CORRECCIÓN: Usar thenReturn() ya que saveAll() no es void.
        // Simulamos que saveAll devuelve la misma lista que se le pasa.
        when(toolRepository.saveAll(ArgumentMatchers.anyList())).thenReturn(foundTools);

        // When
        userServices.Changereplacement_costTool(toolName, adminId, newCost);

        // Then
        assertEquals(newCost, tool1.getReplacement_cost(), "El costo de la herramienta 1 debe actualizarse.");
        assertEquals(newCost, tool2.getReplacement_cost(), "El costo de la herramienta 2 debe actualizarse.");

        // Verificamos que saveAll fue llamado con la lista de herramientas
        verify(toolRepository, times(1)).saveAll(ArgumentMatchers.anyList());
    }

    @Test
    public void testChangereplacement_costTool_whenNotAdmin_thenThrowIllegalArgumentException() {
        // Given
        Long nonAdminId = 2L;
        String toolName = "Martillo";
        int newCost = 200;
        UserEntity nonAdminUser = new UserEntity(nonAdminId, "User", "u@u.com", "pass", "222", USER_STATE_ID, "456", employerRole.getId());

        when(userRepository.findById(nonAdminId)).thenReturn(Optional.of(nonAdminUser));
        when(roleRepository.findById(employerRole.getId())).thenReturn(Optional.of(employerRole));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userServices.Changereplacement_costTool(toolName, nonAdminId, newCost);
        });

        assertEquals("No eres administrador", exception.getMessage());
        verify(toolRepository, never()).saveAll(anyList());
    }

    @Test
    public void testChangereplacement_costTool_whenUserNotFound_thenThrowIllegalArgumentException() {
        // Given
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userServices.Changereplacement_costTool("Martillo", userId, 200);
        });

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(toolRepository, never()).saveAll(anyList());
    }

    // =========================================================================
    // 4. Tests para updateUser
    // =========================================================================

    @Test
    public void testUpdateUser_Success() {
        // Given
        UserEntity userToUpdate = new UserEntity(1L, "User", "u@u.com", "new_pass", "123", USER_STATE_ID, "123", USER_ROLE_ID);
        when(userRepository.save(userToUpdate)).thenReturn(userToUpdate);

        // When
        UserEntity result = userServices.updateUser(userToUpdate);

        // Assert
        assertNotNull(result);
        assertEquals("new_pass", result.getPassword());
        verify(userRepository, times(1)).save(userToUpdate);
    }

    // =========================================================================
    // 5. Tests para deleteUser
    // =========================================================================

    @Test
    public void testDeleteUser_whenUserExists_thenReturnsTrue() throws Exception {
        // Given
        Long userId = 1L;
        // UserEntity con datos dummy para simular la existencia
        UserEntity existingUser = new UserEntity(userId, "Del", "d@d.com", "pass", "123", USER_STATE_ID, "777", USER_ROLE_ID);

        when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        doNothing().when(userRepository).deleteById(userId);

        // When
        boolean result = userServices.deleteUser(userId);

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    public void testDeleteUser_whenUserNotFound_thenThrowException() {
        // Given
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        Exception exception = assertThrows(Exception.class, () -> {
            userServices.deleteUser(userId);
        });

        assertEquals("No existe el usuario", exception.getMessage());
        verify(userRepository, never()).deleteById(userId);
    }

    // =========================================================================
    // 6. Tests para getAllUsers y getUserByRut
    // =========================================================================

    @Test
    public void testGetAllUsers_ReturnsAllUsers() {
        // Given
        List<UserEntity> users = Arrays.asList(
                new UserEntity(1L, "U1", "u1@u.com", "p1", "111", USER_STATE_ID, "123", USER_ROLE_ID),
                new UserEntity(2L, "U2", "u2@u.com", "p2", "222", USER_STATE_ID, "456", USER_ROLE_ID)
        );
        when(userRepository.findAll()).thenReturn(users);

        // When
        List<UserEntity> result = userServices.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testGetUserByRut_UserFound_ReturnsUser() {
        // Given
        String rut = "11122233-4";
        UserEntity expectedUser = new UserEntity(1L, "Found", "f@f.com", "pass", "333", USER_STATE_ID, rut, USER_ROLE_ID);
        when(userRepository.findByRut(rut)).thenReturn(expectedUser);

        // When
        UserEntity result = userServices.getUserByRut(rut);

        // Assert
        assertNotNull(result);
        assertEquals(rut, result.getRut());
        verify(userRepository, times(1)).findByRut(rut);
    }

    @Test
    public void testGetUserByRut_UserNotFound_ReturnsNull() {
        // Given
        String rut = "00000000-0";
        when(userRepository.findByRut(rut)).thenReturn(null);

        // When
        UserEntity result = userServices.getUserByRut(rut);

        // Assert
        assertNull(result);
        verify(userRepository, times(1)).findByRut(rut);
    }
}