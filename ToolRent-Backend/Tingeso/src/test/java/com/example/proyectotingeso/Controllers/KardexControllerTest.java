package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.KardexEntity;
import com.example.proyectotingeso.Services.KardexServices;
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

@WebMvcTest(KardexController.class)
@AutoConfigureMockMvc(addFilters = false) // desactiva seguridad en pruebas
public class KardexControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private KardexServices kardexServices;

    @Test
    @WithMockUser(roles = "USER")
    public void createKardex_ShouldReturnSavedEntity() throws Exception {
        KardexEntity kardex = new KardexEntity(1L, 1L, LocalDate.now(), "juan", 5L, 10);

        given(kardexServices.save(Mockito.any(KardexEntity.class))).willReturn(kardex);

        String json = """
            {
              "StateToolsId": 1,
              "date": "2025-09-10",
              "username": "juan",
              "idtool": 5,
              "quantity": 10
            }
            """;

        mockMvc.perform(post("/api/kardex/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("juan")))
                .andExpect(jsonPath("$.quantity", is(10)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void updateKardex_ShouldReturnUpdatedEntity() throws Exception {
        KardexEntity updated = new KardexEntity(1L, 2L, LocalDate.now(), "maria", 7L, 20);

        given(kardexServices.Update(Mockito.any(KardexEntity.class))).willReturn(updated);

        String json = """
            {
              "id": 1,
              "StateToolsId": 2,
              "date": "2025-09-10",
              "username": "maria",
              "idtool": 7,
              "quantity": 20
            }
            """;

        mockMvc.perform(put("/api/kardex/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("maria")))
                .andExpect(jsonPath("$.quantity", is(20)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getAllKardex_ShouldReturnList() throws Exception {
        KardexEntity k1 = new KardexEntity(1L, 1L, LocalDate.now(), "juan", 5L, 10);
        KardexEntity k2 = new KardexEntity(2L, 2L, LocalDate.now(), "maria", 7L, 20);

        List<KardexEntity> kardexList = Arrays.asList(k1, k2);
        given(kardexServices.findAll()).willReturn(kardexList);

        mockMvc.perform(get("/api/kardex/Allkardex"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("juan")))
                .andExpect(jsonPath("$[1].username", is("maria")));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void deleteKardex_ShouldReturnNoContent() throws Exception {
        Mockito.when(kardexServices.delete(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/kardex/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getRangeKardex_ShouldReturnList() throws Exception {
        KardexEntity k1 = new KardexEntity(1L, 1L, LocalDate.of(2025, 9, 1), "juan", 5L, 10);
        KardexEntity k2 = new KardexEntity(2L, 2L, LocalDate.of(2025, 9, 5), "maria", 7L, 20);

        List<KardexEntity> kardexList = Arrays.asList(k1, k2);
        given(kardexServices.HistoryKardexDateInitandDateFin(
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 9, 10)))
                .willReturn(kardexList);

        mockMvc.perform(get("/api/kardex/Range/{dateinit}/{datefin}", "2025-09-01", "2025-09-10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("juan")))
                .andExpect(jsonPath("$[1].username", is("maria")));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getHistoryByToolName_ShouldReturnList() throws Exception {
        KardexEntity k1 = new KardexEntity(1L, 1L, LocalDate.now(), "juan", 5L, 10);
        KardexEntity k2 = new KardexEntity(2L, 2L, LocalDate.now(), "maria", 5L, 20);

        List<KardexEntity> kardexList = Arrays.asList(k1, k2);
        given(kardexServices.HistoryKardexTool("martillo")).willReturn(kardexList);

        mockMvc.perform(get("/api/kardex/History/{nametool}", "martillo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].idtool", is(5)))
                .andExpect(jsonPath("$[1].idtool", is(5)));
    }
}
