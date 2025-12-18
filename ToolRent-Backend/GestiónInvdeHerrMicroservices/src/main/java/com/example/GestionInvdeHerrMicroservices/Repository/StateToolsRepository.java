package com.example.GestionInvdeHerrMicroservices.Repository;


import com.example.GestionInvdeHerrMicroservices.Entity.StateToolsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StateToolsRepository extends JpaRepository<StateToolsEntity, Long> {

    public StateToolsEntity findByName(String name);

    public Optional<StateToolsEntity> findById(Long id);
}
