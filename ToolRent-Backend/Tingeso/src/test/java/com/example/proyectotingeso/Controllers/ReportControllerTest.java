package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.ReportEntity;
import com.example.proyectotingeso.Services.ReportServices;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportServices reportServices;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createReport_ShouldReturnList() throws Exception {
        LocalDate today = LocalDate.now();

        // Crear los reportes que serán devueltos por el servicio
        ReportEntity r1 = new ReportEntity(1L, "ReportLoanTools", today);
        ReportEntity r2 = new ReportEntity(2L, "ReportLoanTools", today);
        List<ReportEntity> reports = Arrays.asList(r1, r2);

        // Simular la respuesta del servicio
        given(reportServices.ReportLoanTools()).willReturn(reports);

        // Realizar la petición POST y verificar la respuesta
        mockMvc.perform(post("/api/report/ReportLoan")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))  // Esperamos 2 reportes
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getReportByDate_ShouldReturnFilteredReports() throws Exception {
        LocalDate start = LocalDate.of(2025, 9, 1);
        LocalDate end = LocalDate.of(2025, 9, 30);

        // Crear los reportes que serán devueltos por el servicio
        ReportEntity r1 = new ReportEntity(1L, "ReportLoanTools", start);
        ReportEntity r2 = new ReportEntity(2L, "ReportLoanTools", end);
        List<ReportEntity> reports = Arrays.asList(r1, r2);

        // Simular la respuesta del servicio
        given(reportServices.ReportfilterDate(start, end)).willReturn(reports);

        // Realizar la petición GET y verificar la respuesta
        mockMvc.perform(get("/api/report/Reports/{initdate}/{findate}", start, end))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }


    @Test
    @WithMockUser(roles = "USER")
    public void getAllReports_ShouldReturnAllReports() throws Exception {
        LocalDate today = LocalDate.now();

        // Crear los reportes que serán devueltos por el servicio
        ReportEntity r1 = new ReportEntity(1L, "ReportLoanTools", today);
        ReportEntity r2 = new ReportEntity(2L, "ReportLoanTools", today);
        List<ReportEntity> reports = Arrays.asList(r1, r2);

        // Simular la respuesta del servicio
        given(reportServices.ReportLoanTools()).willReturn(reports);

        // Realizar la petición GET y verificar la respuesta
        mockMvc.perform(get("/api/report/AllReports"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createTopToolsReport_ShouldReturnTopToolsReport() throws Exception {
        // Crear los reportes que serán devueltos por el servicio
        ReportEntity r1 = new ReportEntity(1L, "ReportTopTools", LocalDate.now());
        ReportEntity r2 = new ReportEntity(2L, "ReportTopTools", LocalDate.now());
        List<ReportEntity> reports = Arrays.asList(r1, r2);

        // Simular la respuesta del servicio
        given(reportServices.createTopToolsReport()).willReturn(reports);

        // Realizar la petición POST para generar el reporte
        mockMvc.perform(post("/api/report/ReportTopTools")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getAllReportsLoan_ShouldReturnAllReportsLoan() throws Exception {
        LocalDate today = LocalDate.now();

        // Crear los reportes que serán devueltos por el servicio
        ReportEntity r1 = new ReportEntity(1L, "ReportLoanTools", today);
        ReportEntity r2 = new ReportEntity(2L, "ReportLoanTools", today);
        List<ReportEntity> reports = Arrays.asList(r1, r2);

        // Simular la respuesta del servicio
        given(reportServices.GetAllReportLoanTools()).willReturn(reports);

        // Realizar la petición GET y verificar la respuesta
        mockMvc.perform(get("/api/report/AllReportsLoan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getAllReportClientLate_ShouldReturnAllReportsClientLate() throws Exception {
        LocalDate today = LocalDate.now();

        // Crear los reportes que serán devueltos por el servicio
        ReportEntity r1 = new ReportEntity(1L, "ReportClientLoanLate", today);
        ReportEntity r2 = new ReportEntity(2L, "ReportClientLoanLate", today);
        List<ReportEntity> reports = Arrays.asList(r1, r2);

        // Simular la respuesta del servicio
        given(reportServices.GetAllReportClientLoanLate()).willReturn(reports);

        // Realizar la petición GET y verificar la respuesta
        mockMvc.perform(get("/api/report/AllReportClientLate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getAllReportTopTool_ShouldReturnAllReportsTopTool() throws Exception {
        LocalDate today = LocalDate.now();

        // Crear los reportes que serán devueltos por el servicio
        ReportEntity r1 = new ReportEntity(1L, "ReportTopTools", today);
        ReportEntity r2 = new ReportEntity(2L, "ReportTopTools", today);
        List<ReportEntity> reports = Arrays.asList(r1, r2);

        // Simular la respuesta del servicio
        given(reportServices.ReportTopToolsAll()).willReturn(reports);

        // Realizar la petición GET y verificar la respuesta
        mockMvc.perform(get("/api/report/AllReportTopTool"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[1].id", is(2)));
    }


}
