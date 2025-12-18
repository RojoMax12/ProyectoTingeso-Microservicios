package com.example.proyectotingeso.Repository;

import com.example.proyectotingeso.Entity.DataReportEntity;
import com.example.proyectotingeso.Entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataReportRepository extends JpaRepository<DataReportEntity, Long> {

    boolean existsByIdLoanTool(Long idLoanTool);

    boolean existsByIdClient(Long idClient);

    List<DataReportEntity> findByidreport(Long idreport);




}
