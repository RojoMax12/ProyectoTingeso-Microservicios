package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.DataReportEntity;
import com.example.proyectotingeso.Services.DataReportServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DataReportController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DataReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DataReportServices dataReportService;

    // --- CORRECCIÓN: Método Auxiliar Actualizado a la nueva estructura de la Entidad ---
    /**
     * Crea una DataReportEntity simulada basada en la nueva estructura.
     */
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
    // 1. Crear DataReportEntity (POST /)
    // ------------------------------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createDataReport_ShouldReturnCreatedStatus() throws Exception {
        // Arrange
        Long mockIdReport = 100L;
        Long mockIdClient = 1L; // El valor que esperamos
        Long mockIdLoanTool = 10L;
        Long mockIdTool = 5L;
        Long mockNumberTimes = 3L;

        // Entidad que se envía
        DataReportEntity newReport = createMockReport(null, mockIdReport, mockIdClient, mockIdLoanTool, mockIdTool, mockNumberTimes);
        // Entidad que se devuelve con ID asignado
        DataReportEntity savedReport = createMockReport(1L, mockIdReport, mockIdClient, mockIdLoanTool, mockIdTool, mockNumberTimes);

        given(dataReportService.createDataReport(Mockito.any(DataReportEntity.class))).willReturn(savedReport);

        // Act & Assert
        mockMvc.perform(post("/api/DataReport/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newReport)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.idreport", is(mockIdReport.intValue())))

                // 🛑 CORRECCIÓN CLAVE: Cambiar 'idclient' por 'idClient' (Camel Case)
                .andExpect(jsonPath("$.idClient", is(mockIdClient.intValue())));

        verify(dataReportService, times(1)).createDataReport(Mockito.any(DataReportEntity.class));
    }

    // ------------------------------------------------------------------------------------------
    // 2. Obtener DataReportEntity por IDreport (GET /{id})
    // ------------------------------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "USER")
    public void getDataReportByIdreport_ShouldReturnListOfReports() throws Exception {
        // Arrange
        Long targetIdReport = 100L;

        // CORREGIDO: Creación de entidades sin el campo description
        List<DataReportEntity> mockReports = Arrays.asList(
                createMockReport(1L, targetIdReport, 1L, 10L, 5L, 3L),
                createMockReport(2L, targetIdReport, 2L, 20L, 6L, 1L)
        );

        given(dataReportService.findReportByIdreport(targetIdReport)).willReturn(mockReports);

        // Act & Assert
        mockMvc.perform(get("/api/DataReport/{id}", targetIdReport))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(2)))

                // 🛑 CORRECCIÓN 1: Cambiar 'idloantool' por 'idLoanTool' (Camel Case)
                .andExpect(jsonPath("$[0].idLoanTool", is(10)))

                // La segunda aserción ya era correcta:
                .andExpect(jsonPath("$[1].number_of_times_borrowed", is(1)));

        verify(dataReportService, times(1)).findReportByIdreport(targetIdReport);
    }

    // ------------------------------------------------------------------------------------------
    // 3. Obtener todos los DataReportEntity (GET /all)
    // ------------------------------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getAllDataReports_ShouldReturnAllReports() throws Exception {
        // Arrange
        // CORREGIDO: Creación de entidades sin el campo description
        List<DataReportEntity> allReports = Arrays.asList(
                createMockReport(1L, 100L, 1L, 10L, 5L, 3L),
                createMockReport(2L, 200L, 2L, 20L, 6L, 1L)
        );

        given(dataReportService.findAllDataReport()).willReturn(allReports);

        // Act & Assert
        mockMvc.perform(get("/api/DataReport/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()", is(2)))

                // 🛑 CORRECTION: Change 'idloantool' to 'idLoanTool' (Camel Case)
                .andExpect(jsonPath("$[1].idLoanTool", is(20)));

        verify(dataReportService, times(1)).findAllDataReport();
    }
    // ------------------------------------------------------------------------------------------
    // 4. Verificar si existe DataReportEntity por ID de Cliente (GET /existsByClient/{idClient})
    // ------------------------------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "USER")
    public void existsByIdClient_ShouldReturnTrue() throws Exception {
        // Arrange
        Long clientId = 5L;
        given(dataReportService.existsByIdClient(clientId)).willReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/DataReport/existsByClient/{idClient}", clientId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(dataReportService, times(1)).existsByIdClient(clientId);
    }

    @Test
    @WithMockUser(roles = "USER")
    public void existsByIdClient_ShouldReturnFalse() throws Exception {
        // Arrange
        Long clientId = 6L;
        given(dataReportService.existsByIdClient(clientId)).willReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/DataReport/existsByClient/{idClient}", clientId))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));

        verify(dataReportService, times(1)).existsByIdClient(clientId);
    }

    // ------------------------------------------------------------------------------------------
    // 5. Verificar si existe DataReportEntity por ID de LoanTool (GET /existsByLoanTool/{idLoanTool})
    // ------------------------------------------------------------------------------------------

    @Test
    @WithMockUser(roles = "ADMIN")
    public void existsByIdLoanTool_ShouldReturnTrue() throws Exception {
        // Arrange
        Long loanToolId = 20L;
        given(dataReportService.existsByIdLoanTool(loanToolId)).willReturn(true);

        // Act & Assert
        mockMvc.perform(get("/api/DataReport/existsByLoanTool/{idLoanTool}", loanToolId))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(dataReportService, times(1)).existsByIdLoanTool(loanToolId);
    }
}