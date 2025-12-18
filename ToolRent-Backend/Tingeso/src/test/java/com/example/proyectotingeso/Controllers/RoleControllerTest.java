package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.RoleEntity;
import com.example.proyectotingeso.Services.RoleServices;
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
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RolesController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RoleServices roleServices;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createRole_ShouldReturnMessage() throws Exception {
        given(roleServices.createRole()).willReturn("Roles creados correctamente");

        mockMvc.perform(post("/api/roles/")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Roles creados correctamente"));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getAllRoles_ShouldReturnList() throws Exception {
        RoleEntity r1 = new RoleEntity(1L, "Admin");
        RoleEntity r2 = new RoleEntity(2L, "Client");
        List<RoleEntity> roles = Arrays.asList(r1, r2);

        given(roleServices.getAllRoles()).willReturn(roles);

        mockMvc.perform(get("/api/roles/Allroles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Admin")))
                .andExpect(jsonPath("$[1].name", is("Client")));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getRoleById_ShouldReturnRole() throws Exception {
        RoleEntity role = new RoleEntity(1L, "Admin");
        given(roleServices.getRoleById(1L)).willReturn(Optional.of(role));

        mockMvc.perform(get("/api/roles/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Admin")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateRole_ShouldReturnUpdatedRole() throws Exception {
        RoleEntity updatedRole = new RoleEntity(1L, "Employer");
        given(roleServices.updateRole(Mockito.any(RoleEntity.class))).willReturn(updatedRole);

        String json = """
            {
              "id": 1,
              "name": "Employer"
            }
            """;

        mockMvc.perform(put("/api/roles/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Employer")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteRole_ShouldReturnNoContent() throws Exception {
        Mockito.when(roleServices.deleteRole(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/roles/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
