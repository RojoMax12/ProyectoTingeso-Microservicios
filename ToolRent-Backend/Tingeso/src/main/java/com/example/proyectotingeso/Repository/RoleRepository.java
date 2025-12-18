package com.example.proyectotingeso.Repository;

import com.example.proyectotingeso.Entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository  extends JpaRepository<RoleEntity, Long> {

    public RoleEntity findByName(String name);

    public Optional<RoleEntity> findById(Long id);
}
