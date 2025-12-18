package com.example.proyectotingeso.Repository;

import com.example.proyectotingeso.Entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    public List<ReportEntity> findByDateBetweenOrderByDateDesc(LocalDate init, LocalDate fin);


    public List<ReportEntity> findByName(String name);


}
