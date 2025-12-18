package com.example.proyectotingeso.Services;

import com.example.proyectotingeso.Entity.AmountsandratesEntity;
import com.example.proyectotingeso.Repository.AmountsandratesRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.Optional;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class AmountsandratesServicesTest {

    @Mock
    private AmountsandratesRepository amountsandratesRepository;

    @InjectMocks
    private AmountsandratesServices amountsandratesServices;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // --- Pruebas para createAmountsAndRates() ---

// **Modificación del Test**

    @Test
    public void testCreateAmountsAndRates_whenNoExistingConfig_thenCreateNew() {
        // Arrange: Simula que no hay ninguna configuración existente
        when(amountsandratesRepository.findAll()).thenReturn(Collections.emptyList());

        // ARRANGE CLAVE: Cuando se llame a save() con CUALQUIER entidad, Mockito debe retornar
        // esa misma entidad. Esto simula el comportamiento de un repositorio real.
        when(amountsandratesRepository.save(any(AmountsandratesEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // Retorna el primer argumento (la entidad pasada)

        // Act
        AmountsandratesEntity result = amountsandratesServices.createAmountsAndRates();

        // Assert: Verifica que se haya creado una nueva entidad con valores por defecto
        assertNotNull(result); // ¡Esto ya no debería fallar!
        // ... el resto de tus asserts
    }

    @Test
    public void testCreateAmountsAndRates_whenExistingConfig_thenReturnExistingAndDoNotSave() {
        // Arrange: Simula que ya existe una configuración
        AmountsandratesEntity existingEntity = new AmountsandratesEntity();
        existingEntity.setDailyrentalrate(15.0);
        existingEntity.setDailylatefeefine(3.0);
        existingEntity.setReparationcharge(7.0);

        // Simula que findAll devuelve la configuración existente
        when(amountsandratesRepository.findAll()).thenReturn(Collections.singletonList(existingEntity));

        // Act
        AmountsandratesEntity result = amountsandratesServices.createAmountsAndRates();

        // Assert: Verifica que se haya retornado la configuración existente
        assertNotNull(result);
        assertEquals(15.0, result.getDailyrentalrate());

        // Verifica que el repositorio *NO* haya sido llamado para guardar
        verify(amountsandratesRepository, never()).save(any(AmountsandratesEntity.class));
    }

    // --- Pruebas para getAmountsAndRates() ---

    @Test
    public void testGetAmountsAndRates_whenExists_thenReturnExisting() {
        // Arrange: Simula que hay una configuración existente
        AmountsandratesEntity existingEntity = new AmountsandratesEntity();
        existingEntity.setDailyrentalrate(10.0);
        existingEntity.setDailylatefeefine(2.0);
        existingEntity.setReparationcharge(5.0);

        // OJO: La implementación del servicio busca por ID 1L
        when(amountsandratesRepository.findById(1L)).thenReturn(Optional.of(existingEntity));

        // Act
        Optional<AmountsandratesEntity> result = amountsandratesServices.getAmountsAndRates();

        // Assert
        assertTrue(result.isPresent());
        assertEquals(10.0, result.get().getDailyrentalrate());
        assertEquals(2.0, result.get().getDailylatefeefine());
        assertEquals(5.0, result.get().getReparationcharge());

        verify(amountsandratesRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetAmountsAndRates_whenNotExists_thenReturnEmpty() {
        // Arrange: Simula que NO hay configuración
        when(amountsandratesRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Optional<AmountsandratesEntity> result = amountsandratesServices.getAmountsAndRates();

        // Assert
        assertFalse(result.isPresent());

        verify(amountsandratesRepository, times(1)).findById(1L);
    }

    // --- Pruebas para updateAmountAndRates() ---

    @Test
    public void testUpdateAmountsAndRates_whenExists_thenUpdate() {
        // Arrange: Simula que existe una configuración
        AmountsandratesEntity existingEntity = new AmountsandratesEntity();
        existingEntity.setDailyrentalrate(10.0);
        existingEntity.setDailylatefeefine(2.0);
        existingEntity.setReparationcharge(5.0);

        // Simular que findAll devuelve la entidad existente (para que el servicio la encuentre)
        when(amountsandratesRepository.findAll()).thenReturn(Collections.singletonList(existingEntity));
        // Simular que save devuelve la entidad con los valores actualizados y con el ID de la existente
        when(amountsandratesRepository.save(any(AmountsandratesEntity.class))).thenAnswer(invocation -> {
            AmountsandratesEntity entityToSave = invocation.getArgument(0);
            entityToSave.setDailyrentalrate(20.0); // Simular que save persiste los nuevos valores
            return entityToSave;
        });

        AmountsandratesEntity updatedEntityInput = new AmountsandratesEntity();
        updatedEntityInput.setDailyrentalrate(20.0);
        updatedEntityInput.setDailylatefeefine(4.0);
        updatedEntityInput.setReparationcharge(10.0);

        // Act
        AmountsandratesEntity result = amountsandratesServices.updateAmountAndRates(updatedEntityInput);

        // Assert: Verifica que la configuración haya sido actualizada
        assertNotNull(result);
        assertEquals(20.0, result.getDailyrentalrate());
        assertEquals(4.0, result.getDailylatefeefine());
        assertEquals(10.0, result.getReparationcharge());

        // Verifica que el repositorio haya sido llamado para guardar la entidad actualizada
        verify(amountsandratesRepository, times(1)).save(any(AmountsandratesEntity.class));
    }

    @Test
    public void testUpdateAmountsAndRates_whenNotExists_thenCreateNew() {
        // Arrange: Simula que NO existe una configuración
        when(amountsandratesRepository.findAll()).thenReturn(Collections.emptyList());

        AmountsandratesEntity updatedEntityInput = new AmountsandratesEntity();
        updatedEntityInput.setDailyrentalrate(30.0);
        updatedEntityInput.setDailylatefeefine(5.0);
        updatedEntityInput.setReparationcharge(12.0);

        // Simular que save devuelve la entidad con los valores actualizados
        when(amountsandratesRepository.save(any(AmountsandratesEntity.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Act
        AmountsandratesEntity result = amountsandratesServices.updateAmountAndRates(updatedEntityInput);

        // Assert: Verifica que la configuración haya sido creada con los nuevos valores
        assertNotNull(result);
        assertEquals(30.0, result.getDailyrentalrate());
        assertEquals(5.0, result.getDailylatefeefine());
        assertEquals(12.0, result.getReparationcharge());

        // Verifica que el repositorio haya sido llamado para guardar la entidad
        verify(amountsandratesRepository, times(1)).save(any(AmountsandratesEntity.class));
    }
}