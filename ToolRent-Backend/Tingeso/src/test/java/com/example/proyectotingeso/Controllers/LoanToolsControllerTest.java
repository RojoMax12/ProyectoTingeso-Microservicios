package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.LoanToolsEntity;
import com.example.proyectotingeso.Services.LoanToolsServices;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanToolsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LoanToolsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoanToolsServices loanToolsServices;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createLoanTools_ShouldReturnLoan() throws Exception {
        LoanToolsEntity loan = new LoanToolsEntity(1L,
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                1L,
                1L,
                "Active",
                0.0,
                0.0,
                0.0,
                0.0);

        given(loanToolsServices.CreateLoanToolsEntity(Mockito.any(LoanToolsEntity.class))).willReturn(loan);

        String json = """
            {
              "initiallenddate": "2025-09-28",
              "finalreturndate": "2025-10-03",
              "clientid": 1,
              "toolid": 1
            }
            """;

        mockMvc.perform(post("/api/LoanTools/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("Active")))
                .andExpect(jsonPath("$.clientid", is(1)))
                .andExpect(jsonPath("$.toolid", is(1)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getLoanToolsById_ShouldReturnLoan() throws Exception {
        LoanToolsEntity loan = new LoanToolsEntity(1L,
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                1L,
                1L,
                "Active",
                0.0,
                0.0,
                0.0,
                0.0);

        given(loanToolsServices.getLoanToolsEntityById(1L)).willReturn(loan);

        mockMvc.perform(get("/api/LoanTools/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("Active")))
                .andExpect(jsonPath("$.clientid", is(1)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void returnLoanTools_ShouldReturnUpdatedLoan() throws Exception {
        LoanToolsEntity loan = new LoanToolsEntity(1L,
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                1L,
                1L,
                "No active",
                0.0,
                0.0,
                0.0,
                0.0);

        given(loanToolsServices.returnLoanTools(1L, 1L)).willReturn(loan);

        mockMvc.perform(put("/api/LoanTools/return/{iduser}/{idtools}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("No active")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateLoanTools_ShouldReturnLoan() throws Exception {
        LoanToolsEntity loan = new LoanToolsEntity(1L,
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                1L,
                1L,
                "Active",
                0.0,
                0.0,
                0.0,
                0.0);

        given(loanToolsServices.UpdateLoanToolsEntity(Mockito.any(LoanToolsEntity.class))).willReturn(loan);

        String json = """
            {
              "id": 1,
              "initiallenddate": "2025-09-28",
              "finalreturndate": "2025-10-03",
              "clientid": 1,
              "toolid": 1
            }
            """;

        mockMvc.perform(put("/api/LoanTools/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientid", is(1)))
                .andExpect(jsonPath("$.status", is("Active")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteLoanTools_ShouldReturnNoContent() throws Exception {
        Mockito.when(loanToolsServices.DeleteLoanToolsEntity(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/LoanTools/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getLoanToolsByUser_ShouldReturnList() throws Exception {
        LoanToolsEntity l1 = new LoanToolsEntity(1L, LocalDate.now(), LocalDate.now().plusDays(5), 1L, 1L, "Active", 0.0, 0.0, 0.0, 0.0);
        LoanToolsEntity l2 = new LoanToolsEntity(2L, LocalDate.now(), LocalDate.now().plusDays(3), 1L, 2L, "Active", 0.0, 0.0, 0.0, 0.0);
        List<LoanToolsEntity> list = Arrays.asList(l1, l2);

        given(loanToolsServices.getAlluserLoanTools(1L)).willReturn(list);

        mockMvc.perform(get("/api/LoanTools/userloantool/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].toolid", is(1)))
                .andExpect(jsonPath("$[1].toolid", is(2)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void calculateFineLoan_ShouldReturnValue() throws Exception {
        given(loanToolsServices.calculateFine(1L)).willReturn(10.0);

        mockMvc.perform(get("/api/LoanTools/calculate-fine/{loanId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("10.0"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void calculateRentalLoan_ShouldReturnValue() throws Exception {
        given(loanToolsServices.calculateRentalFee(1L)).willReturn(50.0);

        mockMvc.perform(get("/api/LoanTools/calculate-rental/{loanId}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("50.0"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void checkAndUpdateClientStatus_ShouldReturnTrue() throws Exception {
        given(loanToolsServices.checkAndUpdateClientStatus(1L)).willReturn(true);

        mockMvc.perform(put("/api/LoanTools/CheckClient/{idclient}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void payLoanFee_ShouldReturnTrue() throws Exception {
        given(loanToolsServices.registerAllFeesPayment(1L)).willReturn(true);

        mockMvc.perform(put("/api/LoanTools/Pay/{idloan}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    // ... Otros tests

    @Test
    @WithMockUser(roles = "ADMIN") // Añadida para pasar el @PreAuthorize
    public void testRegisterDamageAndReposition_Success() throws Exception {
        // Arrange
        Long loanId = 5L;
        // El servicio no devuelve nada (void), así que no necesitamos mockear un retorno
        doNothing().when(loanToolsServices).registerDamageFeeandReposition(loanId);

        // Act & Assert
        // CORRECCIÓN: Usar la ruta completa /api/LoanTools/register-damage/{idloan}
        mockMvc.perform(put("/api/LoanTools/register-damage/{idloan}", loanId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk()); // Esperamos un 200 OK

        // Verify: Verificamos que el servicio haya sido llamado exactamente una vez con el ID correcto
        verify(loanToolsServices, times(1)).registerDamageFeeandReposition(loanId);
    }


    @Test
    @WithMockUser(roles = "ADMIN")
    public void testRegisterDamageAndReposition_ServiceFails_thenNotFound() throws Exception {
        // Arrange
        Long loanId = 6L;
        // Simular que el servicio lanza una excepción
        doThrow(new RuntimeException("Loan not found")).when(loanToolsServices).registerDamageFeeandReposition(loanId);

        // Act & Assert
        mockMvc.perform(put("/api/LoanTools/register-damage/{idloan}", loanId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                // Cambiar a 404 si el controlador maneja la excepción y la mapea a ese código.
                .andExpect(status().isNotFound());

        verify(loanToolsServices, times(1)).registerDamageFeeandReposition(loanId);
    }
}
