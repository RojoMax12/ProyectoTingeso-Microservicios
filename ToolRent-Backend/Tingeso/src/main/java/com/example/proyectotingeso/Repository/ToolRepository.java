package com.example.proyectotingeso.Repository;

import com.example.proyectotingeso.Entity.ToolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ToolRepository extends JpaRepository<ToolEntity, Long> {

    public ToolEntity findByName(String name);

    Optional<ToolEntity> findFirstByNameOrderByName(String name);


    public List<ToolEntity> findAllByName(String name);

    public Optional<ToolEntity> findById(Long id);

    public List<ToolEntity> findAllByNameAndStates(String name, Long state);
}
