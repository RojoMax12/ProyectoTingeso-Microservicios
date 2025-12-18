package com.example.GestiondekarymovMicroservices.Services;


import com.example.GestiondekarymovMicroservices.Entity.KardexEntity;
import com.example.GestiondekarymovMicroservices.Repository.KardexRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
        List<ToolEntity> tools = ToolRepository.findAllByName(nameTool);

        if (tools == null || tools.isEmpty()) {
            throw new IllegalArgumentException("No existe la herramienta con nombre: " + nameTool);
        }

        List<KardexEntity> fullHistory = new ArrayList<>();

        for (ToolEntity tool : tools) {
            List<KardexEntity> kardex = kardexRepository.findAllByIdtool(tool.getId());
            fullHistory.addAll(kardex);
        }

        return fullHistory;
    }

    public List<Object[]> TopToolKardexTool() {
        List<Object[]> toptool = kardexRepository.getTopTools();
        return toptool;
    }

    public List<KardexEntity> HistoryKardexDateInitandDateFin(LocalDate init, LocalDate fin) {
        return kardexRepository.findByDateBetweenOrderByDateDesc(init, fin);
    }


}
