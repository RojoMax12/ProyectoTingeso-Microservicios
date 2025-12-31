package com.example.ReportyconsMicroservices.Repository;


import com.example.ReportyconsMicroservices.Entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReportRepository extends JpaRepository<ReportEntity, Long> {

    public List<ReportEntity> findByDateBetweenOrderByDateDesc(LocalDate init, LocalDate fin);

    public ReportEntity findByid(Long id);

    public List<ReportEntity> findByName(String name);


}
