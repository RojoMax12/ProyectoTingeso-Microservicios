package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.StateToolsEntity;
import com.example.proyectotingeso.Services.StateToolsServices;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StateToolsController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StateToolsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StateToolsServices stateToolsServices;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createStateTools_ShouldReturnMessage() throws Exception {
        given(stateToolsServices.createStateTools()).willReturn("Estados de herramientas creado");

        mockMvc.perform(post("/api/statetools/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Estados de herramientas creado"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getStateToolsById_ShouldReturnEntity() throws Exception {
        StateToolsEntity state = new StateToolsEntity(1L, "Available");
        given(stateToolsServices.getStateToolsEntityById(1L)).willReturn(state);

        mockMvc.perform(get("/api/statetools/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Available")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateStateTools_ShouldReturnUpdatedEntity() throws Exception {
        StateToolsEntity updated = new StateToolsEntity(1L, "Borrowed");
        given(stateToolsServices.updateStateToolsEntity(Mockito.any(StateToolsEntity.class))).willReturn(updated);

        String json = """
            {
              "id": 1,
              "name": "Borrowed"
            }
            """;

        mockMvc.perform(put("/api/statetools/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Borrowed")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteStateTools_ShouldReturnNoContent() throws Exception {
        Mockito.when(stateToolsServices.deleteStateToolsById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/statetools/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
