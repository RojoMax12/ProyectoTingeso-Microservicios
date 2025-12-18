package com.example.proyectotingeso.Repository;

import com.example.proyectotingeso.Entity.StateUsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StateUsersRepository extends JpaRepository<StateUsersEntity, Long> {

    public StateUsersEntity findByName(String name);
}
