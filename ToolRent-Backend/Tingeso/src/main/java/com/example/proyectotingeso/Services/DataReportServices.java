package com.example.proyectotingeso.Services;

import com.example.proyectotingeso.Entity.DataReportEntity;
import com.example.proyectotingeso.Repository.DataReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
