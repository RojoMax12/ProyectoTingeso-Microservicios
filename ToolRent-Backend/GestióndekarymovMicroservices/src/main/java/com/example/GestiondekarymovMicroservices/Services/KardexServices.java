package com.example.GestiondekarymovMicroservices.Services;


import com.example.GestiondekarymovMicroservices.Entity.KardexEntity;
import com.example.GestiondekarymovMicroservices.Models.Tool;
import com.example.GestiondekarymovMicroservices.Repository.KardexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KardexServices {

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
        List<Tool> tools = restTemplate.getForObject("http://GESTIONINVDEHERRMICROSERVICES/Tools/Allbyname/" + nameTool, List.class);

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
        // 1. Obtenemos los IDs y conteos desde la base de datos de este microservicio
        List<Object[]> results = kardexRepository.getTopToolIdsAndCounts();

        return results.stream().map(result -> {
            Long toolId = ((Number) result[0]).longValue();
            Long total = ((Number) result[1]).longValue();

            // 2. Llamada al microservicio de Tools para obtener los detalles
            // Nota: Asegúrate de tener configurado RestTemplate o usar Feign
            Tool toolData = restTemplate.getForObject(
                    "http://GESTIONINVDEHERRMICROSERVICES/api/Tools/tool/" + toolId,
                    Tool.class
            );

            // 3. Empaquetamos en un Object[] para respetar el tipo de retorno solicitado
            // Índice 0: El objeto Tool completo, Índice 1: El total de préstamos
            return new Object[] { toolData, total };

        }).collect(Collectors.toList());
    }


    public List<KardexEntity> HistoryKardexDateInitandDateFin(LocalDate init, LocalDate fin) {
        return kardexRepository.findByDateBetweenOrderByDateDesc(init, fin);
    }


}
