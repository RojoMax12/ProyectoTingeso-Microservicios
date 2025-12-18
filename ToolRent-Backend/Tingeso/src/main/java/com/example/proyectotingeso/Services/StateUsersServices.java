package com.example.proyectotingeso.Services;


import com.example.proyectotingeso.Entity.StateUsersEntity;
import com.example.proyectotingeso.Repository.StateUsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StateUsersServices {

    @Autowired
    private StateUsersRepository stateUsersRepository;


    public String CreateStateUsers() {
        boolean created = false;
        if (stateUsersRepository.findByName("Active") == null){
            StateUsersEntity stateUsersEntity = new StateUsersEntity(null, "Active");
            stateUsersRepository.save(stateUsersEntity);
            created = true;
        }
        if (stateUsersRepository.findByName("Restricted") == null){
            StateUsersEntity stateUsersEntity = new StateUsersEntity(null, "Restricted");
            stateUsersRepository.save(stateUsersEntity);
            created = true;
        }
        if (created == true){
            return "Estados creados con exito";
        }
        else {
            return "Estados creados";
        }

    }

    public StateUsersEntity getStateUsersById(Long id) {
        return stateUsersRepository.findById(id).orElse(null);
    }

    public List<StateUsersEntity> getAllStateUsers() {
        return stateUsersRepository.findAll();
    }



    public StateUsersEntity updateStateUsers(StateUsersEntity stateUsersEntity) {
        return stateUsersRepository.save(stateUsersEntity);
    }

    public boolean deleteStateUsersById(Long id) throws Exception {
        try {
            stateUsersRepository.deleteById(id);
            return true;

        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
