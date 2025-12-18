package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.UserEntity;
import com.example.proyectotingeso.Services.ToolServices;
import com.example.proyectotingeso.Services.UserServices;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserServices userServices;

    @MockitoBean
    private ToolServices toolServices;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void createUser_ShouldReturnUser() throws Exception {
        UserEntity user = new UserEntity(
                1L,             // id
                "Juan",         // name
                "juan@mail.com",// email
                "1234",         // password
                "12345678",     // phone
                1L,             // state
                "12345678-9",   // rut
                1L              // role
        );

        given(userServices.saveUser(Mockito.any(UserEntity.class))).willReturn(user);

        String json = """
            {
              "name": "Juan",
              "email": "juan@mail.com",
              "password": "1234",
              "phone": "12345678",
              "rut": "12345678-9"
            }
            """;

        mockMvc.perform(post("/api/user/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Juan")))
                .andExpect(jsonPath("$.rut", is("12345678-9")));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getAllUser_ShouldReturnList() throws Exception {
        UserEntity u1 = new UserEntity(1L, "Juan", "juan@mail.com", "1234", "12345678", 1L, "12345678-9", 1L);
        UserEntity u2 = new UserEntity(2L, "Ana", "ana@mail.com", "abcd", "98765432", 1L, "98765432-1", 1L);
        List<UserEntity> users = Arrays.asList(u1, u2);

        // Mockear el servicio
        given(userServices.getAllUsers()).willReturn(new ArrayList<>(users));

        mockMvc.perform(get("/api/user/Alluser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Juan")))
                .andExpect(jsonPath("$[0].rut", is("12345678-9")))
                .andExpect(jsonPath("$[1].name", is("Ana")))
                .andExpect(jsonPath("$[1].rut", is("98765432-1")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateUser_ShouldReturnUpdatedUser() throws Exception {
        UserEntity updated = new UserEntity(1L, "Juan Updated", "juan@mail.com", "1234", "12345678", 1L, "12345678-9", 1L);
        given(userServices.updateUser(Mockito.any(UserEntity.class))).willReturn(updated);

        String json = """
            {
              "id": 1,
              "name": "Juan Updated",
              "email": "juan@mail.com",
              "password": "1234",
              "phone": "12345678",
              "rut": "12345678-9"
            }
            """;

        mockMvc.perform(put("/api/user/UpdateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Juan Updated")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteUser_ShouldReturnNoContent() throws Exception {
        Mockito.when(userServices.deleteUser(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/user/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void login_ShouldReturnTrue() throws Exception {
        UserEntity user = new UserEntity(null, "Juan", "juan@mail.com", "1234", "12345678", 1L, "12345678-9", 1L);
        given(userServices.login(Mockito.any(UserEntity.class))).willReturn(true);

        String json = """
            {
              "rut": "12345678-9",
              "password": "1234"
            }
            """;

        mockMvc.perform(post("/api/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void replacement_ShouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(userServices).Changereplacement_costTool("Martillo", 1L, 5000);

        mockMvc.perform(put("/api/user/replacement/{nametool}/{Userid}/{cost}", "Martillo", 1L, 5000))
                .andExpect(status().isNoContent());
    }
}
