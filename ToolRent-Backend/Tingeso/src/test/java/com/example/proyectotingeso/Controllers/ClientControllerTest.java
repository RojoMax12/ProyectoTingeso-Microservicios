package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.ClientEntity;
import com.example.proyectotingeso.Services.ClientServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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

import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientServices clientServices;

    @Autowired
    private ObjectMapper objectMapper;

    private ClientEntity mockClient1;
    private ClientEntity mockClient2;

    /**
     * Helper para crear instancias mock de ClientEntity.
     * La firma coincide con el constructor generado por Lombok:
     * (id, name, email, rut, phone, state)
     */
    private ClientEntity createMockClient(Long id, String name, String rut) {
        return new ClientEntity(
                id,
                name,
                name.replaceAll("\\s+", "").toLowerCase() + "@test.com", // email generado
                rut,
                "987654321", // phone estático
                1L // state estático
        );
    }

    @BeforeEach
    void setUp() {
        mockClient1 = createMockClient(1L, "Alice Smith", "11111111-1");
        mockClient2 = createMockClient(2L, "Bob Johnson", "22222222-2");
    }

    // =========================================================================
    // 1. POST /api/Client/ -> createClient
    // =========================================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void createClient_ShouldReturnCreatedClient() throws Exception {
        ClientEntity newClientData = createMockClient(null, "Charlie Brown", "33333333-3");
        ClientEntity savedClient = createMockClient(3L, "Charlie Brown", "33333333-3");

        given(clientServices.createClient(Mockito.any(ClientEntity.class))).willReturn(savedClient);

        mockMvc.perform(post("/api/Client/")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newClientData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.name", is("Charlie Brown")));

        verify(clientServices, times(1)).createClient(Mockito.any(ClientEntity.class));
    }

    // =========================================================================
    // 2. GET /api/Client/Allclient -> getAllClient
    // =========================================================================

    @Test
    @WithMockUser(roles = "USER")
    void getAllClient_ShouldReturnListOfClients() throws Exception {
        List<ClientEntity> allClients = Arrays.asList(mockClient1, mockClient2);
        given(clientServices.getAllClients()).willReturn(allClients);

        mockMvc.perform(get("/api/Client/Allclient"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Alice Smith")));

        verify(clientServices, times(1)).getAllClients();
    }

    // =========================================================================
    // 3. GET /api/Client/rut/{rut} -> getClientByRut
    // =========================================================================

    @Test
    @WithMockUser(roles = "USER")
    void getClientByRut_ShouldReturnClient() throws Exception {
        String targetRut = "11111111-1";
        given(clientServices.getClientByRut(targetRut)).willReturn(mockClient1);

        mockMvc.perform(get("/api/Client/rut/{rut}", targetRut))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Alice Smith")));

        verify(clientServices, times(1)).getClientByRut(targetRut);
    }

    @Test
    @WithMockUser(roles = "USER")
    void getClientByRut_NotFound_ShouldReturnEmptyBody() throws Exception {
        String targetRut = "99999999-9";
        given(clientServices.getClientByRut(targetRut)).willReturn(null);

        mockMvc.perform(get("/api/Client/rut/{rut}", targetRut))
                .andExpect(status().isOk())
                .andExpect(content().string(isEmptyOrNullString())); // Verifica 200 OK con cuerpo vacío

        verify(clientServices, times(1)).getClientByRut(targetRut);
    }

    // =========================================================================
    // 4. PUT /api/Client/UpdateClient -> updateClient
    // =========================================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateClient_ShouldReturnUpdatedClient() throws Exception {
        ClientEntity updatedData = createMockClient(1L, "Alice Updated", "11111111-1");
        updatedData.setEmail("new.email@test.com"); // Modificación específica

        given(clientServices.updateClient(Mockito.any(ClientEntity.class))).willReturn(updatedData);

        mockMvc.perform(put("/api/Client/UpdateClient")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Alice Updated")))
                .andExpect(jsonPath("$.email", is("new.email@test.com")));

        verify(clientServices, times(1)).updateClient(Mockito.any(ClientEntity.class));
    }

    // =========================================================================
    // 5. DELETE /api/Client/Deleteclient/{idclient} -> deleteClientId
    // =========================================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteClientId_ShouldReturnNoContent() throws Exception {
        Long clientIdToDelete = 1L;

        // 🛑 CORRECCIÓN: Usar given/willReturn para simular la devolución de un valor.
        // Asumimos que el servicio devuelve 'Boolean'.
        given(clientServices.deleteClient(clientIdToDelete)).willReturn(true);
        // O: doReturn(true).when(clientServices).deleteClient(clientIdToDelete);

        mockMvc.perform(delete("/api/Client/Deleteclient/{idclient}", clientIdToDelete)
                        .with(csrf()))
                .andExpect(status().isNoContent()); // 204 No Content

        verify(clientServices, times(1)).deleteClient(clientIdToDelete);
    }


    // =========================================================================
    // 6. GET /api/Client/{id} -> getClientById
    // =========================================================================

    @Test
    @WithMockUser(roles = "USER")
    void getClientById_ShouldReturnClient() throws Exception {
        Long targetId = 2L;
        given(clientServices.getClientById(targetId)).willReturn(mockClient2);

        mockMvc.perform(get("/api/Client/{id}", targetId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Bob Johnson")));

        verify(clientServices, times(1)).getClientById(targetId);
    }

    // =========================================================================
    // 7. GET /api/Client/AllClientLoanLate -> getAllClientLoanLate
    // =========================================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllClientLoanLate_ShouldReturnLateClients() throws Exception {
        List<ClientEntity> lateClients = Arrays.asList(mockClient1);
        given(clientServices.getAllClientLoanLate()).willReturn(lateClients);

        mockMvc.perform(get("/api/Client/AllClientLoanLate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(clientServices, times(1)).getAllClientLoanLate();
    }
}