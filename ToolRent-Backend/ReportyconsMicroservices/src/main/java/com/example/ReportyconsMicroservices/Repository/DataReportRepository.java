package com.example.ReportyconsMicroservices.Repository;


import com.example.ReportyconsMicroservices.Entity.DataReportEntity;
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
