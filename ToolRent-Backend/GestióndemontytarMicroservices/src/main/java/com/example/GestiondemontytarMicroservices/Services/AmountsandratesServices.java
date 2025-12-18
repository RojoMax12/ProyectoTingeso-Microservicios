package com.example.GestiondemontytarMicroservices.Services;


import com.example.GestiondemontytarMicroservices.Entity.AmountsandratesEntity;
import com.example.GestiondemontytarMicroservices.Repository.AmountsandratesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AmountsandratesServices {

    @Autowired
    private AmountsandratesRepository amountsandratesRepository;

    public AmountsandratesEntity createAmountsAndRates() {
        // Buscar si ya existe alguna configuración (asumiendo que solo debe haber una)
        AmountsandratesEntity existing = amountsandratesRepository.findAll()
                .stream()
                .findFirst()
                .orElse(null);

        if (existing != null) {
            // Ya existe una configuración, la retornamos
            return existing;
        }

        // Si no existe ninguna configuración, creamos una nueva con valores por defecto
        AmountsandratesEntity newEntity = new AmountsandratesEntity();
        newEntity.setDailyrentalrate(0.0);      // Valor por defecto para tarifa diaria
        newEntity.setDailylatefeefine(0.0);     // Valor por defecto para multa diaria
        newEntity.setReparationcharge(0.0);    // Valor por defecto para cargo de reparación

        return amountsandratesRepository.save(newEntity);
    }

    public Optional<AmountsandratesEntity> getAmountsAndRatesbyId(long id) {
        return amountsandratesRepository.findById(id);
    }

    public AmountsandratesEntity updateAmountAndRates(AmountsandratesEntity amountsandratesEntity) {
        // Buscar la configuración existente (asumiendo que solo hay una)
        AmountsandratesEntity existing = amountsandratesRepository.findAll()
                .stream()
                .findFirst()
                .orElse(null);

        if (existing == null) {
            // Si no existe, crear una nueva
            existing = new AmountsandratesEntity();
        }

        // Actualizar los valores
        existing.setDailyrentalrate(amountsandratesEntity.getDailyrentalrate());
        existing.setDailylatefeefine(amountsandratesEntity.getDailylatefeefine());
        existing.setReparationcharge(amountsandratesEntity.getReparationcharge());

        return amountsandratesRepository.save(existing);
    }

    public List<AmountsandratesEntity> getAllAmountsAndRates() {
        return amountsandratesRepository.findAll();
    }

}
