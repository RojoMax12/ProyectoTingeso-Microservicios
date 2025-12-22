package com.example.ReportyconsMicroservices.Services;


import com.example.ReportyconsMicroservices.Entity.DataReportEntity;
import com.example.ReportyconsMicroservices.Repository.DataReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataReportServices {

    @Autowired
    DataReportRepository DataReportRepository;

    public DataReportEntity createDataReport(DataReportEntity DataReportEntity) {
        return DataReportRepository.save(DataReportEntity);
    }

    public List<DataReportEntity> findReportByIdreport(Long id){
        return DataReportRepository.findByidreport(id);
    }

    public List<DataReportEntity> findAllDataReport(){
        return DataReportRepository.findAll();
    }


    public Boolean existsByIdClient(Long idClient) {
        return DataReportRepository.existsByIdClient(idClient);
    }

    public Boolean existsByIdLoanTool(Long idLoanTool) {
        return DataReportRepository.existsByIdLoanTool(idLoanTool);
    }
}
