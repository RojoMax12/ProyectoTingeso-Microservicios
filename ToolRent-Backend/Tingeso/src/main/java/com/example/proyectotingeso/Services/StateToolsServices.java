package com.example.proyectotingeso.Services;

import com.example.proyectotingeso.Entity.StateToolsEntity;
import com.example.proyectotingeso.Repository.StateToolsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class StateToolsServices {

    @Autowired
    StateToolsRepository StateToolsRepository;

    public String createStateTools() {
        boolean created = false;
        if (StateToolsRepository.findByName("Available") == null) {
            StateToolsEntity stateToolsEntity = new StateToolsEntity(null, "Available");
            StateToolsRepository.save(stateToolsEntity);
            created = true;
        }
        if(StateToolsRepository.findByName("Borrowed") == null) {
            StateToolsEntity stateToolsEntity = new StateToolsEntity(null, "Borrowed");
            StateToolsRepository.save(stateToolsEntity);
            created = true;
        }
        if(StateToolsRepository.findByName("In repair") == null) {
            StateToolsEntity stateToolsEntity = new StateToolsEntity(null, "In repair");
            StateToolsRepository.save(stateToolsEntity);
            created = true;
        }
        if(StateToolsRepository.findByName("Discharged") == null) {
            StateToolsEntity stateToolsEntity = new StateToolsEntity(null, "Discharged");
            StateToolsRepository.save(stateToolsEntity);
            created = true;
        }
        if(created) {
            return "Estados de herramientas creado";
        }
        else {
            return "Estados de herramientas ya iniciados";
        }

    }

    public StateToolsEntity getStateToolsEntityById(Long id) {
        return StateToolsRepository.findById(id).orElse(null);
    }

    public StateToolsEntity updateStateToolsEntity(StateToolsEntity stateToolsEntity) {
        return StateToolsRepository.save(stateToolsEntity);
    }

    public boolean deleteStateToolsById(Long id) throws Exception {
        Optional<StateToolsEntity> optionalState = StateToolsRepository.findById(id);

        if (optionalState.isPresent()) {
            StateToolsRepository.deleteById(id);
            return true;
        } else {
            // Lanzar la excepción esperada por el test
            throw new Exception("El estado con ID " + id + " no existe.");

        }
    }
}
