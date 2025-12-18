package com.example.proyectotingeso.Services;

import com.example.proyectotingeso.Entity.DataReportEntity;
import com.example.proyectotingeso.Repository.DataReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DataReportServicesTest {

    // Inyecta los mocks en el servicio que vamos a probar
    @InjectMocks
    private DataReportServices dataReportServices;

    // Crea un mock del repositorio, que es la dependencia
    @Mock
    private DataReportRepository dataReportRepository;

    private DataReportEntity report1;
    private DataReportEntity report2;

    @BeforeEach
    void setUp() {
        // Inicializa los mocks y los inyecta en dataReportServices
        MockitoAnnotations.openMocks(this);

        // Configuración de entidades de prueba basadas en tu estructura
        report1 = createMockReport(1L, 100L, 1L, 10L, 5L, 3L);
        report2 = createMockReport(2L, 100L, 2L, 20L, 6L, 1L);
    }

    // --- Método Auxiliar para crear entidades (similar al del test del repositorio) ---
    private DataReportEntity createMockReport(Long id, Long idReport, Long idClient, Long idLoanTool, Long idTool, Long numberTimesBorrowed) {
        DataReportEntity report = new DataReportEntity();
        report.setId(id);
        report.setIdreport(idReport);
        report.setIdClient(idClient);
        report.setIdLoanTool(idLoanTool);
        report.setIdTool(idTool);
        report.setNumber_of_times_borrowed(numberTimesBorrowed);
        return report;
    }

    // ------------------------------------------------------------------------------------------
    // 1. Test para createDataReport
    // ------------------------------------------------------------------------------------------

    @Test
    void createDataReport_ShouldReturnSavedEntity() {
        // Arrange
        // Simula que el repositorio devuelve la entidad después de guardarla
        when(dataReportRepository.save(report1)).thenReturn(report1);

        // Act
        DataReportEntity result = dataReportServices.createDataReport(report1);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(100L, result.getIdreport());

        // Verifica que el método save del repositorio fue llamado exactamente una vez con la entidad correcta
        verify(dataReportRepository, times(1)).save(report1);
    }

    // ------------------------------------------------------------------------------------------
    // 2. Test para findReportByIdreport
    // ------------------------------------------------------------------------------------------

    @Test
    void findReportByIdreport_ShouldReturnListOfReports() {
        // Arrange
        Long targetIdReport = 100L;
        List<DataReportEntity> mockReports = Arrays.asList(report1, report2);

        // Simula que el repositorio devuelve la lista cuando se llama con el ID
        when(dataReportRepository.findByidreport(targetIdReport)).thenReturn(mockReports);

        // Act
        List<DataReportEntity> result = dataReportServices.findReportByIdreport(targetIdReport);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(targetIdReport, result.get(0).getIdreport());

        // Verifica la llamada al repositorio
        verify(dataReportRepository, times(1)).findByidreport(targetIdReport);
    }

    @Test
    void findReportByIdreport_ShouldReturnEmptyListWhenNotFound() {
        // Arrange
        Long targetIdReport = 999L;
        when(dataReportRepository.findByidreport(targetIdReport)).thenReturn(List.of());

        // Act
        List<DataReportEntity> result = dataReportServices.findReportByIdreport(targetIdReport);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        // Verifica la llamada al repositorio
        verify(dataReportRepository, times(1)).findByidreport(targetIdReport);
    }

    // ------------------------------------------------------------------------------------------
    // 3. Test para findAllDataReport
    // ------------------------------------------------------------------------------------------

    @Test
    void findAllDataReport_ShouldReturnAllReports() {
        // Arrange
        List<DataReportEntity> allReports = Arrays.asList(report1, report2);
        when(dataReportRepository.findAll()).thenReturn(allReports);

        // Act
        List<DataReportEntity> result = dataReportServices.findAllDataReport();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verifica la llamada al repositorio
        verify(dataReportRepository, times(1)).findAll();
    }

    // ------------------------------------------------------------------------------------------
    // 4. Test para existsByIdClient
    // ------------------------------------------------------------------------------------------

    @Test
    void existsByIdClient_ShouldReturnTrue() {
        // Arrange
        Long clientId = 1L;
        when(dataReportRepository.existsByIdClient(clientId)).thenReturn(true);

        // Act
        Boolean exists = dataReportServices.existsByIdClient(clientId);

        // Assert
        assertTrue(exists);

        // Verifica la llamada al repositorio
        verify(dataReportRepository, times(1)).existsByIdClient(clientId);
    }

    @Test
    void existsByIdClient_ShouldReturnFalse() {
        // Arrange
        Long clientId = 99L;
        when(dataReportRepository.existsByIdClient(clientId)).thenReturn(false);

        // Act
        Boolean exists = dataReportServices.existsByIdClient(clientId);

        // Assert
        assertFalse(exists);

        // Verifica la llamada al repositorio
        verify(dataReportRepository, times(1)).existsByIdClient(clientId);
    }

    // ------------------------------------------------------------------------------------------
    // 5. Test para existsByIdLoanTool
    // ------------------------------------------------------------------------------------------

    @Test
    void existsByIdLoanTool_ShouldReturnTrue() {
        // Arrange
        Long loanToolId = 10L;
        when(dataReportRepository.existsByIdLoanTool(loanToolId)).thenReturn(true);

        // Act
        Boolean exists = dataReportServices.existsByIdLoanTool(loanToolId);

        // Assert
        assertTrue(exists);

        // Verifica la llamada al repositorio
        verify(dataReportRepository, times(1)).existsByIdLoanTool(loanToolId);
    }

    @Test
    void existsByIdLoanTool_ShouldReturnFalse() {
        // Arrange
        Long loanToolId = 99L;
        when(dataReportRepository.existsByIdLoanTool(loanToolId)).thenReturn(false);

        // Act
        Boolean exists = dataReportServices.existsByIdLoanTool(loanToolId);

        // Assert
        assertFalse(exists);

        // Verifica la llamada al repositorio
        verify(dataReportRepository, times(1)).existsByIdLoanTool(loanToolId);
    }
}