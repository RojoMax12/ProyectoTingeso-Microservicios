package com.example.proyectotingeso.Repository;

import com.example.proyectotingeso.Entity.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<ClientEntity, Long> {

    public ClientEntity findByRut(String rut);

    public Optional<ClientEntity> findFirstByRut(String rut);

    public Optional<ClientEntity> findFirstByEmail(String email);
}
