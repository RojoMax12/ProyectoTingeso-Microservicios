package com.example.GestiondekarymovMicroservices.Services;


import com.example.GestiondekarymovMicroservices.Entity.KardexEntity;
import com.example.GestiondekarymovMicroservices.Models.Tool;
import com.example.GestiondekarymovMicroservices.Repository.KardexRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KardexServices {

    private static final Logger logger = LoggerFactory.getLogger(KardexServices.class);

    @Autowired
    private KardexRepository kardexRepository;

    @Autowired
    RestTemplate restTemplate;

    public KardexEntity save(KardexEntity kardexEntity) {
        return kardexRepository.save(kardexEntity);
    }

    public List<KardexEntity> findAll() {
        return kardexRepository.findAll();
    }

    public KardexEntity Update(KardexEntity kardexEntity) {
        return kardexRepository.save(kardexEntity);
    }

    public boolean delete(Long id) throws Exception{
        try {
            kardexRepository.deleteById(id);
            return true;
        }
        catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public List<KardexEntity> HistoryKardexTool(String nameTool) {
        Tool[] toolsArray = restTemplate.getForObject(
                "http://m1-inventario-service/api/Tools/Allbyname/" + nameTool,
                Tool[].class);

        List<Tool> tools = Arrays.asList(toolsArray);

        if (tools == null || tools.isEmpty()) {
            throw new IllegalArgumentException("No existe la herramienta con nombre: " + nameTool);
        }

        List<KardexEntity> fullHistory = new ArrayList<>();

        for (Tool tool : tools) {
            List<KardexEntity> kardex = kardexRepository.findAllByIdtool(tool.getId());
            fullHistory.addAll(kardex);
        }

        return fullHistory;
    }



    public List<Object[]> TopToolKardexTool() {
        // 1. Obtener datos locales
        List<Object[]> results = kardexRepository.getTopToolIdsAndCounts();

        if (results == null || results.isEmpty()) {
            logger.info("No se encontraron movimientos en el Kardex para el ranking.");
            return Collections.emptyList();
        }

        return results.stream().map(result -> {
            Long toolId = ((Number) result[0]).longValue();
            Long total = ((Number) result[1]).longValue();

            try {
                // 2. Llamada al microservicio de Inventario
                // Sugerencia: Si es posible, crea un endpoint en Tools que reciba una lista de IDs
                Tool toolData = restTemplate.getForObject(
                        "http://m1-inventario-service/api/Tools/tool/" + toolId,
                        Tool.class
                );

                logger.debug("Herramienta recuperada: {} (ID: {})", toolData != null ? toolData.getName() : "N/A", toolId);

                return new Object[] { toolId, toolData != null ? toolData.getName() : "Desconocida", total };
            } catch (RestClientException e) {
                logger.error("Error al conectar con m1-inventario-service para ID {}: {}", toolId, e.getMessage());
                // Retornamos datos parciales para no romper todo el reporte
                return new Object[] { toolId, "Error al cargar nombre", total };
            }
        }).collect(Collectors.toList());
    }


    public List<KardexEntity> HistoryKardexDateInitandDateFin(LocalDate init, LocalDate fin) {
        return kardexRepository.findByDateBetweenOrderByDateDesc(init, fin);
    }


}
