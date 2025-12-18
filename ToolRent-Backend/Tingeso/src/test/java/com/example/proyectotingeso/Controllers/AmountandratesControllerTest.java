package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.AmountsandratesEntity;
import com.example.proyectotingeso.Services.AmountsandratesServices;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AmountandratesController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AmountandratesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AmountsandratesServices amountsandratesServices;

    // Test para crear los valores predeterminados de Amounts and Rates
    @Test
    @WithMockUser(roles = "ADMIN") // Simula un usuario con rol ADMIN
    public void createAmountsAndRates_ShouldReturnDefaultValues() throws Exception {
        // Definir el objeto AmountsandratesEntity que se devolverá al realizar la creación
        AmountsandratesEntity entity = new AmountsandratesEntity(
                1L,
                0.0,  // dailyrentalrate
                0.0,  // dailylatefeefine
                0.0   // reparationcharge
        );

        // Simular la respuesta del servicio de creación de Amountsandrates
        given(amountsandratesServices.createAmountsAndRates()).willReturn(entity);

        // Llamada al endpoint POST
        mockMvc.perform(post("/api/AmountandRates/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dailyrentalrate\": 0.0, \"dailylatefeefine\": 0.0, \"reparationcharge\": 0.0}"))
                .andExpect(status().isOk()) // Verificar que el estado sea 200 OK
                .andExpect(jsonPath("$.dailyrentalrate", is(0.0))) // Verificar el valor de dailyrentalrate
                .andExpect(jsonPath("$.dailylatefeefine", is(0.0))) // Verificar el valor de dailylatefeefine
                .andExpect(jsonPath("$.reparationcharge", is(0.0))); // Verificar el valor de reparationcharge
    }

    // Test para actualizar los valores de Amounts and Rates
    @Test
    @WithMockUser(roles = "ADMIN") // Simula un usuario con rol ADMIN
    public void updateAmountsAndRates_ShouldReturnUpdatedEntity() throws Exception {
        // Definir el objeto actualizado que se devolverá al realizar la actualización
        AmountsandratesEntity updated = new AmountsandratesEntity(
                1L,
                25.0,  // dailyrentalrate
                12.0,  // dailylatefeefine
                5.0    // reparationcharge
        );

        // Simular la respuesta del servicio de actualización de Amountsandrates
        given(amountsandratesServices.updateAmountAndRates(Mockito.any(AmountsandratesEntity.class)))
                .willReturn(updated);

        // Definir el JSON que se enviará en la solicitud PUT
        String requestJson = """
            {
                "id": 1,
                "dailyrentalrate": 25.0,
                "dailylatefeefine": 12.0,
                "reparationcharge": 5.0
            }
            """;

        // Llamada al endpoint PUT
        mockMvc.perform(put("/api/AmountandRates/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk()) // Verificar que el estado sea 200 OK
                .andExpect(jsonPath("$.dailyrentalrate", is(25.0))) // Verificar el valor actualizado de dailyrentalrate
                .andExpect(jsonPath("$.dailylatefeefine", is(12.0))) // Verificar el valor actualizado de dailylatefeefine
                .andExpect(jsonPath("$.reparationcharge", is(5.0))); // Verificar el valor actualizado de reparationcharge
    }
}

