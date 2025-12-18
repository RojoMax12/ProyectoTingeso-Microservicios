package com.example.proyectotingeso.Services;

import com.example.proyectotingeso.Entity.*;
import com.example.proyectotingeso.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.ArgumentMatchers;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


public class LoanToolsServicesTest {

    @Mock
    private LoanToolsRepository loanToolsRepository;

    @Mock
    private ToolRepository toolRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private AmountsandratesRepository amountsandratesRepository;

    @Mock
    private StateToolsRepository stateToolsRepository;

    @Mock
    private StateUsersRepository stateUsersRepository;

    @Mock private Clock clock;

    // Se usa spy en lugar de @InjectMocks para poder mockear métodos internos como calculateRentalFee
    @InjectMocks
    private LoanToolsServices loanToolsServices = spy(new LoanToolsServices());

    private StateToolsEntity availableState;
    private StateToolsEntity borrowedState;
    private StateUsersEntity restrictedUserState;
    private StateUsersEntity activeUserState;

    @BeforeEach
    public void setup() {
        // Asegurarse de que el spy de loanToolsServices use los mocks
        MockitoAnnotations.openMocks(this);

        // Inicialización y Mocking de Estados Requeridos por el Servicio
        availableState = new StateToolsEntity(1L, "Disponible");
        borrowedState = new StateToolsEntity(2L, "Prestado");
        activeUserState = new StateUsersEntity(1L, "Active");
        restrictedUserState = new StateUsersEntity(2L, "Restricted");

        // Mocking para los estados de herramienta (usados en la validación y el try/catch)
        when(stateToolsRepository.findAll()).thenReturn(Arrays.asList(availableState, borrowedState));

        // Mocking para los estados de usuario (usados en la validación del cliente)
        when(stateUsersRepository.findByName("Restricted")).thenReturn(restrictedUserState);
        when(stateUsersRepository.findByName("Active")).thenReturn(activeUserState);

        // Mocking de los métodos internos espiados
        doReturn(false).when(loanToolsServices).hasOverdueLoans(anyLong());
        doReturn(1).when(loanToolsServices).countActiveLoans(anyLong()); // Valor por defecto bajo

        // Configuración por defecto para el guardado (retorna el argumento 0)
        when(loanToolsRepository.save(any(LoanToolsEntity.class))).thenAnswer(i -> {
            LoanToolsEntity saved = i.getArgument(0);
            if (saved.getId() == null) saved.setId(1L); // Asignar ID si es nuevo
            return saved;
        });
    }

    private void mockCurrentDate(LocalDate fixedDate) {
        Clock fixedClock = Clock.fixed(
                fixedDate.atStartOfDay(ZoneId.systemDefault()).toInstant(),
                ZoneId.systemDefault()
        );
        when(clock.getZone()).thenReturn(fixedClock.getZone());
        when(clock.instant()).thenReturn(fixedClock.instant());
    }

    // --- Pruebas de CREACIÓN (CREATE) ---

    @Test
    public void testCreateLoanToolsEntity_Success() {
        // Arrange
        Long clientId = 1L;
        Long toolId = 10L;

        // Asumo que 100 es replacement_cost y 1L es state.
        ToolEntity availableTool = new ToolEntity(toolId, "Martillo", "Herramienta", 100, 1L);
        LoanToolsEntity newLoan = new LoanToolsEntity(null, LocalDate.now(), LocalDate.now().plusDays(7), clientId, toolId, "Active", 0.0, 0.0, 0.0, 0.0);

        // 1. Cliente y Herramienta
        ClientEntity client = new ClientEntity();
        client.setId(clientId);
        // CORRECCIÓN: Establecer el estado del cliente para evitar NullPointerException
        client.setState(1L); // Asumimos 1L es el estado "Active" o no restringido

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(toolRepository.findById(toolId)).thenReturn(Optional.of(availableTool));

        // 2. Estados de Herramienta (1L = Disponible, 2L = Prestado)
        StateToolsEntity availableState = new StateToolsEntity(1L, "Disponible");
        StateToolsEntity borrowedState = new StateToolsEntity(2L, "Prestado");
        when(stateToolsRepository.findAll()).thenReturn(Arrays.asList(availableState, borrowedState));

        // 2.5 Mockear los estados de Usuario (Añadido en el paso anterior para evitar NPE)
        StateUsersEntity restrictedUserState = new StateUsersEntity(2L, "Restricted");
        when(stateUsersRepository.findByName("Restricted")).thenReturn(restrictedUserState);

        // 3. Préstamos activos (ninguno para pasar el límite y la validación de duplicados)
        when(loanToolsRepository.findAllByClientid(clientId)).thenReturn(Collections.emptyList());
        when(loanToolsRepository.findAllByClientidAndStatus(clientId, "Active")).thenReturn(Collections.emptyList());

        // 4. Simular guardado del préstamo (asignar ID)
        when(loanToolsRepository.save(any(LoanToolsEntity.class))).thenAnswer(i -> {
            LoanToolsEntity saved = i.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // 5. Mockear el método calculateRentalFee para que devuelva un double
        doReturn(100.0).when(loanToolsServices).calculateRentalFee(anyLong());

        // Act
        LoanToolsEntity result = loanToolsServices.CreateLoanToolsEntity(newLoan);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Active", result.getStatus());

        // Verificar interacciones y estados
        verify(toolRepository, times(1)).save(availableTool);
        verify(loanToolsRepository, times(1)).save(newLoan);
        verify(loanToolsServices, times(1)).calculateRentalFee(1L);
        assertEquals(2L, availableTool.getStates()); // Herramienta debe estar en estado 'Prestado' (2L)
    }

    // --- Pruebas de MULTA (Fine) ---

    @Test
    public void testCalculateFine_LoanIsOnTime_thenZeroFineAndStatusActive() {
        // Arrange
        Long loanId = 2L;
        LocalDate finalReturnDate = LocalDate.now().plusDays(5);
        LoanToolsEntity loanToolsEntity = new LoanToolsEntity(loanId, LocalDate.now().minusDays(2), finalReturnDate, 1L, 1L, "Active", 10.0, 0.0, 0.0, 0.0);

        when(loanToolsRepository.findById(loanId)).thenReturn(Optional.of(loanToolsEntity));
        when(loanToolsRepository.save(any(LoanToolsEntity.class))).thenReturn(loanToolsEntity);

        // Act
        double result = loanToolsServices.calculateFine(loanId);

        // Assert
        assertEquals(0.0, result);
        assertEquals(0.0, loanToolsEntity.getLateFee());
        assertEquals("Active", loanToolsEntity.getStatus());

        // Verifica que se haya guardado el préstamo con los valores actualizados
        verify(loanToolsRepository, times(1)).save(loanToolsEntity);
        // Verifica que NO se haya llamado a obtener tarifas ni a bloquear cliente
        verify(amountsandratesRepository, never()).findAll();
        verify(clientRepository, never()).findById(anyLong());
    }

    // El test testCalculateFine_whenLoanIsLate_thenCalculateFine ya está cubierto.

    // --- Pruebas de TARIFA DE ALQUILER (Rental Fee) ---

    @Test
    public void testCalculateRentalFee_Success() {
        // Arrange
        Long loanId = 3L;
        LocalDate startDate = LocalDate.now().minusDays(2);
        LocalDate endDate = LocalDate.now().plusDays(5); // 7 días de préstamo (endDate - startDate)
        LoanToolsEntity loan = new LoanToolsEntity(loanId, startDate, endDate, 1L, 1L, "Active", 0.0, 0.0, 0.0, 0.0);

        AmountsandratesEntity rates = new AmountsandratesEntity();
        rates.setDailyrentalrate(10.0); // $10.0 por día

        // Simular repositorio
        when(loanToolsRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(amountsandratesRepository.findAll()).thenReturn(List.of(rates));
        when(loanToolsRepository.save(any(LoanToolsEntity.class))).thenReturn(loan);

        // Act
        double result = loanToolsServices.calculateRentalFee(loanId);

        // Assert
        // Días de préstamo: 7. Tarifa: 7 * 10.0 = 70.0
        assertEquals(70.0, result);
        assertEquals(70.0, loan.getRentalFee());

        verify(loanToolsRepository, times(1)).save(loan);
    }

    // --- Pruebas de DEVOLUCIÓN (Return) ---

    @Test
    public void testReturnLoanTools_Success() {
        // Arrange
        Long userId = 1L;
        Long toolId = 10L;

        ToolEntity tool = new ToolEntity(toolId, "Martillo", "Herramienta", 300, 2L); // Estado 2L: Prestado
        LoanToolsEntity loan = new LoanToolsEntity(1L, LocalDate.now().minusDays(5), LocalDate.now().plusDays(2), userId, toolId, "Active", 0.0, 0.0, 0.0, 0.0);

        // Estados de herramienta (1L = Disponible, 2L = Prestado)
        StateToolsEntity availableState = new StateToolsEntity(1L, "Disponible");
        StateToolsEntity borrowedState = new StateToolsEntity(2L, "Prestado");
        when(stateToolsRepository.findAll()).thenReturn(Arrays.asList(availableState, borrowedState));

        // Comportamiento del repositorio
        when(toolRepository.findById(toolId)).thenReturn(Optional.of(tool));
        when(loanToolsRepository.findByClientidAndToolid(userId, toolId)).thenReturn(Optional.of(loan));
        when(loanToolsRepository.save(any(LoanToolsEntity.class))).thenReturn(loan);

        // Mockear checkAndUpdateClientStatus (simular que no lanza excepción)
        doReturn(true).when(loanToolsServices).checkAndUpdateClientStatus(userId);

        // Act
        LoanToolsEntity result = loanToolsServices.returnLoanTools(userId, toolId);

        // Assert
        assertNotNull(result);
        assertEquals(1L, tool.getStates()); // Estado de la herramienta debe ser "Disponible" (1L)
        assertEquals("No active", result.getStatus()); // Estado del préstamo debe ser "No active"

        // Verificar interacciones
        verify(toolRepository, times(1)).save(tool);
        verify(loanToolsServices, times(1)).checkAndUpdateClientStatus(userId);
        verify(loanToolsRepository, times(1)).save(loan);
    }

    // --- Pruebas de ESTADO DE CLIENTE (checkAndUpdateClientStatus) ---

    @Test
    public void testCheckAndUpdateClientStatus_RestrictedAndClear_BecomesActive() {
        // Arrange
        Long clientId = 1L;
        ClientEntity client = new ClientEntity();
        client.setId(clientId);
        client.setState(2L); // 2L = Restricted

        // Estados de usuario
        StateUsersEntity restricted = new StateUsersEntity(2L, "Restricted");
        StateUsersEntity active = new StateUsersEntity(1L, "Active");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(stateUsersRepository.findByName("Restricted")).thenReturn(restricted);
        when(stateUsersRepository.findByName("Active")).thenReturn(active);

        // Simular NO préstamos vencidos
        doReturn(false).when(loanToolsServices).hasOverdueLoans(clientId);

        // Simular NO multas impagas
        LoanToolsEntity cleanLoan = new LoanToolsEntity();
        cleanLoan.setLateFee(0.0);
        cleanLoan.setDamageFee(0.0);
        cleanLoan.setRepositionFee(0.0);
        when(loanToolsRepository.findAllByClientid(clientId)).thenReturn(Arrays.asList(cleanLoan));

        // Act
        boolean result = loanToolsServices.checkAndUpdateClientStatus(clientId);

        // Assert
        assertTrue(result); // Se desbloqueó
        assertEquals(1L, client.getState()); // El estado debe ser Active
        verify(clientRepository, times(1)).save(client);
    }

    @Test
    public void testCheckAndUpdateClientStatus_RestrictedButUnpaidFees_RemainsRestricted() {
        // Arrange
        Long clientId = 1L;
        ClientEntity client = new ClientEntity();
        client.setId(clientId);
        client.setState(2L); // 2L = Restricted

        // Estados de usuario
        StateUsersEntity restricted = new StateUsersEntity(2L, "Restricted");
        StateUsersEntity active = new StateUsersEntity(1L, "Active");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(stateUsersRepository.findByName("Restricted")).thenReturn(restricted);
        when(stateUsersRepository.findByName("Active")).thenReturn(active);

        // Simular NO préstamos vencidos
        doReturn(false).when(loanToolsServices).hasOverdueLoans(clientId);

        // Simular multas impagas
        LoanToolsEntity fineLoan = new LoanToolsEntity();
        fineLoan.setLateFee(15.0); // Multa impaga
        fineLoan.setDamageFee(0.0);
        fineLoan.setRepositionFee(0.0);
        when(loanToolsRepository.findAllByClientid(clientId)).thenReturn(Arrays.asList(fineLoan));

        // Act
        boolean result = loanToolsServices.checkAndUpdateClientStatus(clientId);

        // Assert
        assertFalse(result); // No se desbloqueó
        assertEquals(2L, client.getState()); // El estado debe permanecer Restricted
        verify(clientRepository, never()).save(client); // No se guarda el cliente
    }

    // --- Pruebas de REGISTRO DE CARGOS POR DAÑO/REPOSICIÓN ---

    @Test
    public void testRegisterDamageFeeandReposition_State3_RepositionFee_Calculated() {
        // Arrange
        Long loanId = 1L;
        Long toolId = 10L;
        double reparationCharge = 50.0;

        LoanToolsEntity loan = new LoanToolsEntity(loanId, LocalDate.now(), LocalDate.now().plusDays(7), 1L, toolId, "Active", 0.0, 0.0, 0.0, 0.0);
        ToolEntity tool = new ToolEntity(toolId, "Martillo", "Herramienta", 3000, 3L); // Estado 3: Reparación

        AmountsandratesEntity rates = new AmountsandratesEntity();
        rates.setReparationcharge(reparationCharge);

        // Comportamiento del repositorio
        when(loanToolsRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(toolRepository.findById(toolId)).thenReturn(Optional.of(tool));
        when(amountsandratesRepository.findById(3L)).thenReturn(Optional.of(rates)); // ID 3L en el servicio
        when(loanToolsRepository.save(any(LoanToolsEntity.class))).thenReturn(loan);

        // Act
        loanToolsServices.registerDamageFeeandReposition(loanId);

        // Assert
        assertEquals(reparationCharge, loan.getRepositionFee());
        assertEquals(0.0, loan.getDamageFee()); // El otro fee debe ser 0.0
        verify(loanToolsRepository, times(1)).save(loan);
    }

    @Test
    public void testRegisterDamageFeeandReposition_State4_DamageFee_Calculated() {
        // Arrange
        Long loanId = 2L;
        Long toolId = 11L;
        int replacementCost = 350;

        LoanToolsEntity loan = new LoanToolsEntity(loanId, LocalDate.now(), LocalDate.now().plusDays(7), 1L, toolId, "Active", 0.0, 0.0, 0.0, 0.0);
        ToolEntity tool = new ToolEntity(toolId, "Sierra", "Herramienta", replacementCost, 4L); // Estado 4: Dañada

        AmountsandratesEntity rates = new AmountsandratesEntity(); // Necesario para evitar NullPointerException si se llama a rates.getReparationcharge()
        when(amountsandratesRepository.findById(3L)).thenReturn(Optional.of(rates));

        // Comportamiento del repositorio
        when(loanToolsRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(toolRepository.findById(toolId)).thenReturn(Optional.of(tool));
        when(loanToolsRepository.save(any(LoanToolsEntity.class))).thenReturn(loan);

        // Act
        loanToolsServices.registerDamageFeeandReposition(loanId);

        // Assert
        assertEquals(0.0, loan.getRepositionFee()); // El otro fee debe ser 0.0
        assertEquals((double) replacementCost, loan.getDamageFee());
        verify(loanToolsRepository, times(1)).save(loan);
    }

    // --- Pruebas CRUD Simples y Queries ---

    @Test
    public void testUpdateLoanToolsEntity_Success() {
        // Arrange
        Long loanId = 1L;
        LoanToolsEntity original = new LoanToolsEntity(loanId, LocalDate.now(), LocalDate.now().plusDays(7), 1L, 10L, "Active", 0.0, 0.0, 0.0, 0.0);
        LoanToolsEntity updated = new LoanToolsEntity(loanId, LocalDate.now(), LocalDate.now().plusDays(14), 1L, 10L, "Active", 0.0, 0.0, 0.0, 0.0);

        when(loanToolsRepository.save(ArgumentMatchers.any(LoanToolsEntity.class))).thenReturn(updated);

        // Act
        LoanToolsEntity result = loanToolsServices.UpdateLoanToolsEntity(updated);

        // Assert
        assertNotNull(result);
        assertEquals(updated.getFinalreturndate(), result.getFinalreturndate());
        verify(loanToolsRepository, times(1)).save(updated);
    }

    @Test
    public void testDeleteLoanToolsEntity_Success() throws Exception {
        // Arrange
        Long loanId = 1L;
        doNothing().when(loanToolsRepository).deleteById(loanId);

        // Act
        boolean result = loanToolsServices.DeleteLoanToolsEntity(loanId);

        // Assert
        assertTrue(result);
        verify(loanToolsRepository, times(1)).deleteById(loanId);
    }

    @Test
    public void testGetAlluserLoanTools_Success() {
        // Arrange
        Long userId = 1L;
        List<LoanToolsEntity> mockLoans = Arrays.asList(new LoanToolsEntity(), new LoanToolsEntity());
        when(loanToolsRepository.findAllByClientid(userId)).thenReturn(mockLoans);

        // Act
        List<LoanToolsEntity> result = loanToolsServices.getAlluserLoanTools(userId);

        // Assert
        assertEquals(2, result.size());
        verify(loanToolsRepository, times(1)).findAllByClientid(userId);
    }

    @Test
    public void testFindAllLoansToolStatusAndRentalFee_Success() {
        // Arrange
        List<String> statuses = List.of("Late", "Active");
        List<LoanToolsEntity> mockLoans = Arrays.asList(new LoanToolsEntity(), new LoanToolsEntity());
        when(loanToolsRepository.findAllBystatusInAndRentalFeeGreaterThan(statuses, 0.0)).thenReturn(mockLoans);

        // Act
        List<LoanToolsEntity> result = loanToolsServices.findallloanstoolstatusandRentalFee();

        // Assert
        assertEquals(2, result.size());
        verify(loanToolsRepository, times(1)).findAllBystatusInAndRentalFeeGreaterThan(statuses, 0.0);
    }

    @Test
    public void testFindAllLoansToolStatusLate_Success() {
        // Arrange
        List<LoanToolsEntity> mockLoans = Arrays.asList(new LoanToolsEntity());
        when(loanToolsRepository.findAllBystatus("Late")).thenReturn(mockLoans);

        // Act
        List<LoanToolsEntity> result = loanToolsServices.findallloanstoolstatusLate();

        // Assert
        assertEquals(1, result.size());
        verify(loanToolsRepository, times(1)).findAllBystatus("Late");
    }

    // ... (código existente de LoanToolsServicesTest)

    // --- Pruebas de MULTA (Fine) Faltantes ---

    @Test
    public void testCalculateFine_LoanIsLate_thenCalculateFineAndBlockClient() {
        // Arrange
        Long loanId = 2L;
        Long clientId = 1L;
        LocalDate today = LocalDate.now();
        LocalDate finalReturnDate = today.minusDays(3); // 3 días de atraso

        LoanToolsEntity loan = new LoanToolsEntity(loanId, today.minusDays(5), finalReturnDate, clientId, 1L, "Active", 10.0, 0.0, 0.0, 0.0);
        ClientEntity client = new ClientEntity(clientId, "Activo", "johan", "21163", "569", 1L); // Cliente activo inicialmente

        AmountsandratesEntity rates = new AmountsandratesEntity();
        rates.setDailylatefeefine(5.0); // $5.0 de multa diaria (3 días * $5.0 = $15.0)

        StateUsersEntity restrictedState = new StateUsersEntity(2L, "Restricted");

        when(loanToolsRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(amountsandratesRepository.findAll()).thenReturn(List.of(rates));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(stateUsersRepository.findByName("Restricted")).thenReturn(restrictedState);
        when(loanToolsRepository.save(any(LoanToolsEntity.class))).thenReturn(loan);

        // Act
        double result = loanToolsServices.calculateFine(loanId);

        // Assert
        assertEquals(15.0, result);
        assertEquals(15.0, loan.getLateFee());
        assertEquals("Late", loan.getStatus());

        // El cliente debe estar bloqueado (estado 2L)
        assertEquals(2L, client.getState());

        // Verifica interacciones
        verify(loanToolsRepository, times(1)).save(loan);
        verify(clientRepository, times(1)).save(client);
    }

    @Test
    public void testCalculateFine_NoRatesConfigured_thenThrowException() {
        // Arrange
        Long loanId = 2L;
        LocalDate finalReturnDate = LocalDate.now().minusDays(3);
        LoanToolsEntity loanToolsEntity = new LoanToolsEntity(loanId, LocalDate.now().minusDays(5), finalReturnDate, 1L, 1L, "Active", 10.0, 0.0, 0.0, 0.0);

        when(loanToolsRepository.findById(loanId)).thenReturn(Optional.of(loanToolsEntity));
        // Simula que no hay tarifas configuradas
        when(amountsandratesRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            loanToolsServices.calculateFine(loanId);
        });

        assertTrue(exception.getMessage().contains("Tarifas no configuradas"));
    }

    @Test
    public void testCheckAndUpdateClientStatus_ActiveAndClear_RemainsActive() {
        // Arrange
        Long clientId = 1L;
        ClientEntity client = new ClientEntity();
        client.setId(clientId);
        client.setState(1L); // 1L = Active (no restringido)

        StateUsersEntity restricted = new StateUsersEntity(2L, "Restricted");
        StateUsersEntity active = new StateUsersEntity(1L, "Active");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(stateUsersRepository.findByName("Restricted")).thenReturn(restricted);
        when(stateUsersRepository.findByName("Active")).thenReturn(active);

        // Act
        boolean result = loanToolsServices.checkAndUpdateClientStatus(clientId);

        // Assert
        assertFalse(result); // No se realiza ningún cambio (no estaba restringido)
        assertEquals(1L, client.getState()); // El estado se mantiene Active
        verify(clientRepository, never()).save(client); // No se debe guardar
    }

    @Test
    public void testCheckAndUpdateClientStatus_RestrictedButHasOverdueLoan_RemainsRestricted() {
        // Arrange
        Long clientId = 1L;
        ClientEntity client = new ClientEntity();
        client.setId(clientId);
        client.setState(2L); // 2L = Restricted

        StateUsersEntity restricted = new StateUsersEntity(2L, "Restricted");
        StateUsersEntity active = new StateUsersEntity(1L, "Active");

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(stateUsersRepository.findByName("Restricted")).thenReturn(restricted);
        when(stateUsersRepository.findByName("Active")).thenReturn(active);

        // Simular que TIENE préstamos vencidos
        doReturn(true).when(loanToolsServices).hasOverdueLoans(clientId);

        // Simular préstamos (aunque la primera condición ya lo bloquea)
        when(loanToolsRepository.findAllByClientid(clientId)).thenReturn(Collections.emptyList());

        // Act
        boolean result = loanToolsServices.checkAndUpdateClientStatus(clientId);

        // Assert
        assertFalse(result); // Permanece restringido
        assertEquals(2L, client.getState()); // El estado se mantiene Restricted
        verify(clientRepository, never()).save(client);
    }

    // --- Pruebas de PAGO DE MULTAS (registerAllFeesPayment) ---

    @Test
    public void testRegisterAllFeesPayment_Success() {
        // Arrange
        Long loanId = 1L;
        Long clientId = 1L;

        // Préstamo con multas pendientes
        LoanToolsEntity loan = new LoanToolsEntity(loanId, LocalDate.now(), LocalDate.now().plusDays(5), clientId, 1L, "Late", 10.0, 15.0, 50.0, 20.0);

        when(loanToolsRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(loanToolsRepository.save(any(LoanToolsEntity.class))).thenReturn(loan);

        // Mockear checkAndUpdateClientStatus para que no lance excepción
        doReturn(true).when(loanToolsServices).checkAndUpdateClientStatus(clientId);

        // Act
        Boolean result = loanToolsServices.registerAllFeesPayment(loanId);

        // Assert
        assertTrue(result);
        assertEquals(0.0, loan.getLateFee());
        assertEquals(0.0, loan.getDamageFee());
        assertEquals(0.0, loan.getRepositionFee());
        assertEquals(0.0, loan.getRentalFee());
        assertEquals("No active", loan.getStatus());

        // Verificar interacciones
        verify(loanToolsRepository, times(1)).save(loan);
        verify(loanToolsServices, times(1)).checkAndUpdateClientStatus(clientId);
    }

    // --- Pruebas de CARGO POR DAÑO/REPOSICIÓN (registerDamageFeeandReposition) ---

    @Test
    public void testRegisterDamageFeeandReposition_ToolInRepair_RegistersRepositionFee() {
        // Arrange
        Long loanId = 1L;
        Long toolId = 10L;
        LoanToolsEntity loan = new LoanToolsEntity(loanId, LocalDate.now(), LocalDate.now().plusDays(5), 1L, toolId, "Active", 0.0, 0.0, 0.0, 0.0);

        // Herramienta en estado 3 (Reparación)
        ToolEntity tool = new ToolEntity(toolId, "Sierra", "Madera", 500, 3L);

        AmountsandratesEntity rates = new AmountsandratesEntity();
        rates.setReparationcharge(75.0); // Tarifa de reparación

        when(loanToolsRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(toolRepository.findById(toolId)).thenReturn(Optional.of(tool));
        // Mockear la búsqueda de tarifas por ID (se busca 3L en el servicio)
        when(amountsandratesRepository.findById(3L)).thenReturn(Optional.of(rates));
        when(loanToolsRepository.save(any(LoanToolsEntity.class))).thenReturn(loan);

        // Act
        loanToolsServices.registerDamageFeeandReposition(loanId);

        // Assert
        assertEquals(75.0, loan.getRepositionFee());
        assertEquals(0.0, loan.getDamageFee(), "El cargo por daño debe ser 0.0");
        verify(loanToolsRepository, times(1)).save(loan);
    }

    @Test
    public void testRegisterDamageFeeandReposition_ToolDamaged_RegistersDamageFee() {
        // Arrange
        Long loanId = 1L;
        Long toolId = 10L;
        LoanToolsEntity loan = new LoanToolsEntity(loanId, LocalDate.now(), LocalDate.now().plusDays(5), 1L, toolId, "Active", 0.0, 0.0, 0.0, 0.0);

        // Herramienta en estado 4 (Dañada/Pérdida) con costo de reposición de 500
        ToolEntity tool = new ToolEntity(toolId, "Sierra", "Madera", 500, 4L);

        AmountsandratesEntity rates = new AmountsandratesEntity();
        rates.setReparationcharge(75.0); // Aunque esté mockeada, no se usa en este caso

        when(loanToolsRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(toolRepository.findById(toolId)).thenReturn(Optional.of(tool));
        when(amountsandratesRepository.findById(3L)).thenReturn(Optional.of(rates));
        when(loanToolsRepository.save(any(LoanToolsEntity.class))).thenReturn(loan);

        // Act
        loanToolsServices.registerDamageFeeandReposition(loanId);

        // Assert
        assertEquals(500.0, loan.getDamageFee(), "El cargo por daño debe ser el costo de reposición.");
        assertEquals(0.0, loan.getRepositionFee(), "El cargo por reparación debe ser 0.0");
        verify(loanToolsRepository, times(1)).save(loan);
    }


        @Test
        public void testCreateLoanToolsEntity_NullLoanEntity_thenThrowException() {
            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                loanToolsServices.CreateLoanToolsEntity(null);
            });
            verify(loanToolsRepository, never()).save(any());
        }

        @Test
        public void testCreateLoanToolsEntity_NullToolId_thenThrowException() {
            // Arrange
            LoanToolsEntity loan = new LoanToolsEntity(null, LocalDate.now(), LocalDate.now().plusDays(7), 1L, null, "Pending", 0.0, 0.0, 0.0, 0.0);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                loanToolsServices.CreateLoanToolsEntity(loan);
            });
            assertTrue(exception.getMessage().contains("ID de la herramienta no puede ser nulo"));
            verify(loanToolsRepository, never()).save(any());
        }

        @Test
        public void testCreateLoanToolsEntity_NullInitialDate_thenThrowException() {
            // Arrange
            LoanToolsEntity loan = new LoanToolsEntity(null, null, LocalDate.now().plusDays(7), 1L, 10L, "Pending", 0.0, 0.0, 0.0, 0.0);

            // Configuración mínima para pasar las validaciones de ID y Cliente/Herramienta
            when(clientRepository.findById(1L)).thenReturn(Optional.of(new ClientEntity(1L, "Juan", "a","a","a", 1L)));
            when(toolRepository.findById(10L)).thenReturn(Optional.of(new ToolEntity(10L, "T", "C", 100, availableState.getId())));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                loanToolsServices.CreateLoanToolsEntity(loan);
            });
            assertTrue(exception.getMessage().contains("fecha de inicio no puede ser nula"));
            verify(loanToolsRepository, never()).save(any());
        }


        // --- 3. Tests de Restricciones del Cliente ---

        @Test
        public void testCreateLoanToolsEntity_ClientHasOverdueLoans_thenThrowException() {
            // Arrange
            Long clientId = 1L;
            LoanToolsEntity newLoan = new LoanToolsEntity(null, LocalDate.now(), LocalDate.now().plusDays(7), clientId, 10L, "Pending", 0.0, 0.0, 0.0, 0.0);

            // Simular que el cliente tiene préstamos vencidos
            doReturn(true).when(loanToolsServices).hasOverdueLoans(clientId);

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                loanToolsServices.CreateLoanToolsEntity(newLoan);
            });
            assertTrue(exception.getMessage().contains("Cliente bloqueado por préstamos vencidos pendientes"));
            verify(loanToolsRepository, never()).save(any());
        }

        @Test
        public void testCreateLoanToolsEntity_ClientLimitExceeded_thenThrowException() {
            // Arrange
            Long clientId = 1L;
            LoanToolsEntity newLoan = new LoanToolsEntity(null, LocalDate.now(), LocalDate.now().plusDays(7), clientId, 10L, "Pending", 0.0, 0.0, 0.0, 0.0);

            // Configuración mínima
            when(clientRepository.findById(clientId)).thenReturn(Optional.of(new ClientEntity(clientId, "Juan", "a", "a", "a", activeUserState.getId())));

            // Simular que el cliente ya tiene 6 préstamos activos (Límite 5)
            doReturn(6).when(loanToolsServices).countActiveLoans(clientId);

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                loanToolsServices.CreateLoanToolsEntity(newLoan);
            });

            assertTrue(exception.getMessage().contains("ya tiene el máximo de 5 préstamos vigentes"));
            verify(loanToolsRepository, never()).save(any(LoanToolsEntity.class));
        }

        @Test
        public void testCreateLoanToolsEntity_ClientIsRestricted_thenThrowException() {
            // Arrange
            Long clientId = 1L;
            Long toolId = 10L;

            // Cliente en estado Restringido (ID 2L)
            ClientEntity restrictedClient = new ClientEntity(clientId, "Juan", "a", "a", "a",restrictedUserState.getId());

            LoanToolsEntity newLoan = new LoanToolsEntity(null, LocalDate.now(), LocalDate.now().plusDays(7), clientId, toolId, "Pending", 0.0, 0.0, 0.0, 0.0);

            when(clientRepository.findById(clientId)).thenReturn(Optional.of(restrictedClient));
            when(toolRepository.findById(toolId)).thenReturn(Optional.of(new ToolEntity(toolId, "Martillo", "Herramienta", 100, availableState.getId())));

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                loanToolsServices.CreateLoanToolsEntity(newLoan);
            });

            assertTrue(exception.getMessage().contains("Cliente en estado restringido"));
            verify(loanToolsRepository, never()).save(any(LoanToolsEntity.class));
        }

        @Test
        public void testCreateLoanToolsEntity_ClientHasSameToolType_thenThrowException() {
            // Arrange
            Long clientId = 1L;
            Long newToolId = 10L;
            Long existingToolId = 11L;

            LoanToolsEntity newLoan = new LoanToolsEntity(null, LocalDate.now(), LocalDate.now().plusDays(7), clientId, newToolId, "Pending", 0.0, 0.0, 0.0, 0.0);
            LoanToolsEntity existingLoan = new LoanToolsEntity(100L, LocalDate.now(), LocalDate.now().plusDays(5), clientId, existingToolId, "Active", 0.0, 0.0, 0.0, 0.0);

            // Cliente y Herramientas (mismo nombre y categoría)
            ClientEntity client = new ClientEntity(clientId, "Juan", "a", "a", "a",activeUserState.getId());

            ToolEntity newTool = new ToolEntity(newToolId, "Martillo", "Herramientas de mano", 100, availableState.getId());
            ToolEntity existingTool = new ToolEntity(existingToolId, "Martillo", "Herramientas de mano", 100, 2L); // Estado prestado

            when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
            when(toolRepository.findById(newToolId)).thenReturn(Optional.of(newTool));
            when(toolRepository.findById(existingToolId)).thenReturn(Optional.of(existingTool));
            when(loanToolsRepository.findAllByClientidAndStatus(clientId, "Active")).thenReturn(Arrays.asList(existingLoan));

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                loanToolsServices.CreateLoanToolsEntity(newLoan);
            });

            assertTrue(exception.getMessage().contains("El cliente ya tiene una herramienta prestada con el nombre 'Martillo' en la categoría 'Herramientas de mano'"));
            verify(loanToolsRepository, never()).save(any(LoanToolsEntity.class));
        }


        // --- 4. Tests de Restricciones de Herramienta/Estados ---

        @Test
        public void testCreateLoanToolsEntity_ToolNotFound_thenThrowException() {
            // Arrange
            Long clientId = 1L;
            Long toolId = 99L;

            LoanToolsEntity newLoan = new LoanToolsEntity(null, LocalDate.now(), LocalDate.now().plusDays(7), clientId, toolId, "Pending", 0.0, 0.0, 0.0, 0.0);

            when(clientRepository.findById(clientId)).thenReturn(Optional.of(new ClientEntity(clientId, "Juan", "a", "a", "a",activeUserState.getId())));
            when(toolRepository.findById(toolId)).thenReturn(Optional.empty()); // Herramienta no encontrada

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                loanToolsServices.CreateLoanToolsEntity(newLoan);
            });

            // CORRECCIÓN: Esperar el mensaje de la primera validación
            assertTrue(exception.getMessage().contains("Herramienta no encontrada"));

            // Verificar que NUNCA se llama a la segunda búsqueda (toolRepository.findById)
            // Ya se llamó una vez en la línea 82, no se necesita verificar 'never'.
            // verify(toolRepository, times(1)).findById(toolId); <-- Opcional para verificar la única llamada
            verify(loanToolsRepository, never()).save(any(LoanToolsEntity.class));
        }

        @Test
        public void testCreateLoanToolsEntity_ToolNotAvailable_thenThrowException() {
            // Arrange
            Long clientId = 1L;
            Long toolId = 10L;

            // Herramienta en estado 3L (Por ejemplo, Reparación)
            ToolEntity unavailableTool = new ToolEntity(toolId, "Martillo", "Herramienta", 100, 3L);

            LoanToolsEntity newLoan = new LoanToolsEntity(null, LocalDate.now(), LocalDate.now().plusDays(7), clientId, toolId, "Pending", 0.0, 0.0, 0.0, 0.0);

            when(clientRepository.findById(clientId)).thenReturn(Optional.of(new ClientEntity(clientId, "Juan", "a", "a", "a",activeUserState.getId())));
            when(toolRepository.findById(toolId)).thenReturn(Optional.of(unavailableTool));

            // Act & Assert
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
                loanToolsServices.CreateLoanToolsEntity(newLoan);
            });

            assertTrue(exception.getMessage().contains("La herramienta no está disponible"));
            verify(loanToolsRepository, never()).save(any(LoanToolsEntity.class));
        }


        // --- 5. Test de Manejo de Errores con Rollback ---

        @Test
        public void testCreateLoanToolsEntity_SaveFails_ThenRollbackToolState() {
            // Arrange
            Long clientId = 1L;
            Long toolId = 10L;

            ToolEntity availableTool = new ToolEntity(toolId, "Martillo", "Herramienta", 100, availableState.getId());
            ClientEntity client = new ClientEntity(clientId, "Juan","a","a","a", activeUserState.getId());
            LoanToolsEntity newLoan = new LoanToolsEntity(null, LocalDate.now(), LocalDate.now().plusDays(7), clientId, toolId, "Pending", 0.0, 0.0, 0.0, 0.0);

            when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
            when(toolRepository.findById(toolId)).thenReturn(Optional.of(availableTool));
            when(loanToolsRepository.findAllByClientidAndStatus(clientId, "Active")).thenReturn(Collections.emptyList());

            // 1. Simular que la herramienta se guarda correctamente al cambiar su estado (Paso 8)
            when(toolRepository.save(availableTool)).thenReturn(availableTool);

            // 2. Simular que el guardado del préstamo (loanToolsRepository.save) falla (Paso 9)
            when(loanToolsRepository.save(newLoan)).thenThrow(new RuntimeException("Simulated DB error"));

            // Act & Assert
            RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                loanToolsServices.CreateLoanToolsEntity(newLoan);
            });

            assertTrue(exception.getMessage().contains("Error al crear el préstamo"));

            // Assert Rollback:
            // 1. Verificar que se intentó guardar el préstamo y falló (Paso 9)
            verify(loanToolsRepository, times(1)).save(newLoan);

            // 2. Verificar que se intentó guardar la herramienta con el estado prestado (Paso 8)
            // 3. Verificar que se revirtió el estado de la herramienta (Rollback en el catch)
            verify(toolRepository, times(2)).save(availableTool);

            // 4. El estado final de la herramienta DEBE ser el original (Disponible, 1L)
            assertEquals(availableState.getId(), availableTool.getStates(), "El estado de la herramienta debe ser restaurado a Disponible (1L).");
        }

    @Test
    public void testGetLoanToolsEntityById_Success() {
        // Arrange
        Long loanId = 5L;
        LoanToolsEntity expectedLoan = new LoanToolsEntity(loanId, LocalDate.now(), LocalDate.now().plusDays(5), 1L, 10L, "Active", 0.0, 0.0, 0.0, 0.0);

        // Simular que el repositorio encuentra el préstamo
        when(loanToolsRepository.findById(loanId)).thenReturn(Optional.of(expectedLoan));

        // Act
        LoanToolsEntity result = loanToolsServices.getLoanToolsEntityById(loanId);

        // Assert
        assertNotNull(result, "El resultado no debe ser nulo.");
        assertEquals(loanId, result.getId(), "El ID del préstamo devuelto debe coincidir.");

        // Verificar que el método del repositorio fue llamado
        verify(loanToolsRepository, times(1)).findById(loanId);
    }

// -------------------------------------------------------------------------------------------------------------------------

    @Test
    public void testGetLoanToolsEntityById_NotFound_ThrowsNoSuchElementException() {
        // Arrange
        Long loanId = 99L;

        // Simular que el repositorio NO encuentra el préstamo y devuelve Optional.empty()
        when(loanToolsRepository.findById(loanId)).thenReturn(Optional.empty());

        // Act & Assert
        // Esperamos que se lance NoSuchElementException debido al .get() en Optional.empty()
        assertThrows(NoSuchElementException.class, () -> {
            loanToolsServices.getLoanToolsEntityById(loanId);
        });

        // Verificar que el método del repositorio fue llamado
        verify(loanToolsRepository, times(1)).findById(loanId);
    }

    @Test
    public void testCountActiveLoans_ClientHasMultipleActiveLoans_ReturnsCorrectCount() {
        // Arrange
        Long clientId = 1L;

        doCallRealMethod().when(loanToolsServices).countActiveLoans(clientId);

        // Crear una lista simulada de préstamos activos (Status = "Active")
        LoanToolsEntity loan1 = new LoanToolsEntity(1L, LocalDate.now(), LocalDate.now().plusDays(5), clientId, 10L, "Active", 0.0, 0.0, 0.0, 0.0);
        LoanToolsEntity loan2 = new LoanToolsEntity(2L, LocalDate.now(), LocalDate.now().plusDays(7), clientId, 11L, "Active", 0.0, 0.0, 0.0, 0.0);
        LoanToolsEntity loan3 = new LoanToolsEntity(3L, LocalDate.now(), LocalDate.now().plusDays(10), clientId, 12L, "Active", 0.0, 0.0, 0.0, 0.0);

        List<LoanToolsEntity> activeLoans = Arrays.asList(loan1, loan2, loan3);

        // Simular que el repositorio devuelve 3 préstamos con estado "Active"
        when(loanToolsRepository.findAllByClientidAndStatus(clientId, "Active")).thenReturn(activeLoans);

        // Act
        int result = loanToolsServices.countActiveLoans(clientId);

        // Assert
        assertEquals(3, result, "El conteo debe ser 3, correspondiente a los préstamos activos simulados.");
        verify(loanToolsRepository, times(1)).findAllByClientidAndStatus(clientId, "Active");
    }

// -------------------------------------------------------------------------------------------------------------------------

    @Test
    public void testCountActiveLoans_ClientHasZeroActiveLoans_ReturnsZero() {
        // Arrange
        Long clientId = 2L;

        doCallRealMethod().when(loanToolsServices).countActiveLoans(clientId);

        // 1. Corregido: Simular que el repositorio devuelve una lista vacía para el estado "Active"
        when(loanToolsRepository.findAllByClientidAndStatus(clientId, "Active")).thenReturn(Collections.emptyList());

        // Act
        int result = loanToolsServices.countActiveLoans(clientId);

        // Assert
        assertEquals(0, result, "El conteo debe ser 0 si no hay préstamos con estado 'Active'.");
        // La verificación ahora usará el estado correcto
        verify(loanToolsRepository, times(1)).findAllByClientidAndStatus(clientId, "Active");
    }
// -------------------------------------------------------------------------------------------------------------------------

    @Test
    public void testCountActiveLoans_ClientHasOtherStatusLoans_ReturnsZeroActive() {
        // Arrange
        Long clientId = 3L;

        doCallRealMethod().when(loanToolsServices).countActiveLoans(clientId);

        // Crear una lista simulada con préstamos que tienen otros estados (e.g., "Finished", "Late")
        LoanToolsEntity finishedLoan = new LoanToolsEntity(10L, LocalDate.now().minusDays(30), LocalDate.now().minusDays(25), clientId, 20L, "Finished", 0.0, 0.0, 0.0, 0.0);
        LoanToolsEntity lateLoan = new LoanToolsEntity(11L, LocalDate.now().minusDays(10), LocalDate.now().minusDays(5), clientId, 21L, "Late", 15.0, 0.0, 0.0, 0.0);

        // NOTA: Este test asume que la consulta findAllByClientidAndStatus(clientId, "Active")
        // es la única que se ejecuta, y que el repositorio no devuelve los préstamos "Finished" o "Late".
        // Esto es correcto para un test unitario, ya que solo probamos la interacción con el mock.

        when(loanToolsRepository.findAllByClientidAndStatus(clientId, "Active")).thenReturn(Collections.emptyList());

        // Act
        int result = loanToolsServices.countActiveLoans(clientId);

        // Assert
        assertEquals(0, result, "El conteo debe ser 0, ya que el repositorio solo fue consultado por préstamos 'Active' y devolvió una lista vacía.");
        verify(loanToolsRepository, times(1)).findAllByClientidAndStatus(clientId, "Active");
    }
}
