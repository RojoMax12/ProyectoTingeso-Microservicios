package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.StateUsersEntity;
import com.example.proyectotingeso.Services.StateUsersServices;
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

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StateUsersController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StateUsersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StateUsersServices stateUsersServices;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createStateUser_ShouldReturnMessage() throws Exception {
        given(stateUsersServices.CreateStateUsers()).willReturn("Estados creados con exito");

        mockMvc.perform(post("/api/stateuser/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Estados creados con exito"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getStateUserById_ShouldReturnEntity() throws Exception {
        StateUsersEntity state = new StateUsersEntity(1L, "Active");
        given(stateUsersServices.getStateUsersById(1L)).willReturn(state);

        mockMvc.perform(get("/api/stateuser/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Active")));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getAllStateUsers_ShouldReturnList() throws Exception {
        StateUsersEntity s1 = new StateUsersEntity(1L, "Active");
        StateUsersEntity s2 = new StateUsersEntity(2L, "Restricted");
        List<StateUsersEntity> states = Arrays.asList(s1, s2);

        given(stateUsersServices.getAllStateUsers()).willReturn(states);

        mockMvc.perform(get("/api/stateuser/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Active")))
                .andExpect(jsonPath("$[1].name", is("Restricted")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateStateUser_ShouldReturnUpdatedEntity() throws Exception {
        StateUsersEntity updated = new StateUsersEntity(1L, "Restricted");
        given(stateUsersServices.updateStateUsers(Mockito.any(StateUsersEntity.class))).willReturn(updated);

        String json = """
            {
              "id": 1,
              "name": "Restricted"
            }
            """;

        mockMvc.perform(put("/api/stateuser/UpdateStateUsers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Restricted")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteStateUser_ShouldReturnNoContent() throws Exception {
        Mockito.when(stateUsersServices.deleteStateUsersById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/stateuser/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
