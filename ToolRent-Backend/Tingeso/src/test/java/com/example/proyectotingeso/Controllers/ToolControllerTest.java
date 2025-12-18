package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.ToolEntity;
import com.example.proyectotingeso.Services.ToolServices;
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

@WebMvcTest(ToolController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ToolControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ToolServices toolServices;

    @Test
    @WithMockUser(roles = "ADMIN")
    public void addTool_ShouldReturnTool() throws Exception {
        ToolEntity tool = new ToolEntity(1L, "Martillo", "Manuales", 5000, 1L);
        given(toolServices.save(Mockito.any(ToolEntity.class))).willReturn(tool);

        String json = """
            {
              "name": "Martillo",
              "category": "Manuales",
              "replacement_cost": 5000
            }
            """;

        mockMvc.perform(post("/api/Tools/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Martillo")))
                .andExpect(jsonPath("$.category", is("Manuales")));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getAllTools_ShouldReturnList() throws Exception {
        ToolEntity t1 = new ToolEntity(1L, "Martillo", "Manuales", 5000, 1L);
        ToolEntity t2 = new ToolEntity(2L, "Taladro", "Electricas", 20000, 1L);
        List<ToolEntity> tools = Arrays.asList(t1, t2);
        given(toolServices.getAlltool()).willReturn(tools);

        mockMvc.perform(get("/api/Tools/alltools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Martillo")))
                .andExpect(jsonPath("$[1].name", is("Taladro")));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getToolById_ShouldReturnTool() throws Exception {
        ToolEntity tool = new ToolEntity(1L, "Martillo", "Manuales", 5000, 1L);
        given(toolServices.getTool(1L)).willReturn(tool);

        mockMvc.perform(get("/api/Tools/tool/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Martillo")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void updateTool_ShouldReturnUpdatedTool() throws Exception {
        ToolEntity updated = new ToolEntity(1L, "Martillo XL", "Manuales", 7000, 1L);
        given(toolServices.updateTool(Mockito.any(ToolEntity.class))).willReturn(updated);

        String json = """
            {
              "id": 1,
              "name": "Martillo XL",
              "category": "Manuales",
              "replacement_cost": 7000
            }
            """;

        mockMvc.perform(put("/api/Tools/UpdateTool")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Martillo XL")))
                .andExpect(jsonPath("$.replacement_cost", is(7000)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteTool_ShouldReturnNoContent() throws Exception {
        Mockito.when(toolServices.deletetoolbyid(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/Tools/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    public void getInventory_ShouldReturnCount() throws Exception {
        ToolEntity tool = new ToolEntity(1L, "Martillo", "Manuales", 5000, 1L);
        given(toolServices.inventory(Mockito.any(ToolEntity.class))).willReturn(5);

        String json = """
            {
              "name": "Martillo",
              "category": "Manuales",
              "replacement_cost": 5000
            }
            """;

        mockMvc.perform(get("/api/Tools/inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void unsubscribeTool_ShouldReturnTool() throws Exception {
        ToolEntity tool = new ToolEntity(1L, "Martillo", "Manuales", 5000, 4L); // 4L = Discharged
        given(toolServices.unsubscribeToolAdmin(1L)).willReturn(tool);

        mockMvc.perform(put("/api/Tools/{idtool}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.states", is(4)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void borrowedTool_ShouldReturnTool() throws Exception {
        ToolEntity tool = new ToolEntity(1L, "Martillo", "Manuales", 5000, 2L); // 2L = Borrowed
        given(toolServices.borrowedTool(1L)).willReturn(tool);

        mockMvc.perform(put("/api/Tools/Borrowed/{idtool}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.states", is(2)));
    }

    @Test
    @WithMockUser(roles = "USER")
    public void inRepairTool_ShouldReturnTool() throws Exception {
        ToolEntity tool = new ToolEntity(1L, "Martillo", "Manuales", 5000, 3L); // 3L = In repair
        given(toolServices.inrepair(1L)).willReturn(tool);

        mockMvc.perform(put("/api/Tools/inrepair/{idtool}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.states", is(3)));
    }
}
