package com.example.proyectotingeso.Services;

import com.example.proyectotingeso.Entity.ClientEntity;
import com.example.proyectotingeso.Entity.DataReportEntity;
import com.example.proyectotingeso.Entity.LoanToolsEntity;
import com.example.proyectotingeso.Entity.ReportEntity;
import com.example.proyectotingeso.Repository.DataReportRepository;
import com.example.proyectotingeso.Repository.ReportRepository;
import com.fasterxml.jackson.core.util.RecyclerPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServices {

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    DataReportRepository dataReportRepository;

    @Autowired
    LoanToolsServices loanToolsServices;

    @Autowired
    KardexServices kardexServices;

    @Autowired
    DataReportServices dataReportServices;

    @Autowired
    private ClientServices clientServices;


    public ReportEntity createReport(ReportEntity reportEntity) {
        return reportRepository.save(reportEntity);
    }

    public List<ReportEntity> ReportLoanTools() {
        // Obtener los préstamos con estado y tarifa de alquiler
        List<LoanToolsEntity> loantools = loanToolsServices.findallloanstoolstatusandRentalFee();
        System.out.println("Préstamos encontrados: " + loantools);
        LocalDate date = LocalDate.now();

        // Crear un único ReportEntity para todos los préstamos
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setName("ReportLoanTools");
        reportEntity.setDate(date);

        // Guardar el reporte primero
        ReportEntity savedReport = reportRepository.save(reportEntity);

        // Lista para almacenar los DataReportEntity relacionados
        List<DataReportEntity> dataReportEntities = new ArrayList<>();

        // Ahora no se filtra, se crean reportes para todos los préstamos
        loantools.forEach(loan -> {
            // Crear nueva instancia de DataReportEntity
            DataReportEntity dataReportEntity = new DataReportEntity();
            dataReportEntity.setIdLoanTool(loan.getId());
            dataReportEntity.setIdreport(savedReport.getId()); // Asignar el id del reporte guardado

            // Agregar a la lista de DataReportEntity
            dataReportEntities.add(dataReportEntity);
        });

        // Guardar todos los DataReportEntity relacionados con el reporte
        dataReportRepository.saveAll(dataReportEntities);

        return List.of(savedReport); // Retornar solo el reporte creado
    }

    public List<ReportEntity> ReportClientLoanLate() {
        // Obtener los clientes con préstamos con retraso
        List<ClientEntity> clients = clientServices.getAllClientLoanLate();

        // Crear un único ReportEntity para todos los clientes
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setName("ReportClientLoanLate");
        reportEntity.setDate(LocalDate.now());

        // Guardar el reporte primero
        ReportEntity savedReport = reportRepository.save(reportEntity);

        // Lista para almacenar los DataReportEntity relacionados
        List<DataReportEntity> dataReportEntities = new ArrayList<>();

        // Ahora no se filtra, se crean reportes para todos los clientes
        clients.forEach(client -> {
            // Crear nueva instancia de DataReportEntity
            DataReportEntity dataReportEntity = new DataReportEntity();
            dataReportEntity.setIdClient(client.getId());
            dataReportEntity.setIdreport(savedReport.getId()); // Asignar el id del reporte guardado

            // Agregar a la lista de DataReportEntity
            dataReportEntities.add(dataReportEntity);
        });

        // Guardar todos los DataReportEntity relacionados con el reporte
        dataReportRepository.saveAll(dataReportEntities);

        return List.of(savedReport); // Retornar solo el reporte creado
    }


    public List<ReportEntity> createTopToolsReport() {
        // Obtener el ranking de las herramientas
        List<Object[]> ranking = kardexServices.TopToolKardexTool();

        // Imprimir las filas de manera más clara
        for (Object[] row : ranking) {
            Long toolId = (Long) row[0]; // idTool
            String toolName = (String) row[1]; // Nombre de la herramienta
            Long timesBorrowed = (Long) row[2]; // Número de veces prestada

            // Imprimir en consola con formato claro
            System.out.println(String.format("ID: %d, Herramienta: %s, Veces Prestada: %d", toolId, toolName, timesBorrowed));
        }

        List<ReportEntity> createdReports = new ArrayList<>();

        // Crear un único ReportEntity para todos los préstamos
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setName("ReportTopTools");
        reportEntity.setDate(LocalDate.now());

        // Guardar el reporte primero
        ReportEntity savedReport = reportRepository.save(reportEntity);

        // Lista para almacenar los DataReportEntity relacionados
        List<DataReportEntity> dataReportEntities = new ArrayList<>();

        // Iterar sobre el ranking para crear los DataReportEntity
        for (Object[] row : ranking) {
            Long toolId = (Long) row[0];  // idTool
            String toolName = (String) row[1];  // Nombre de la herramienta
            Long number_of_times_borrowed = (Long) row[2]; // Número de veces prestada

            // Crear nueva instancia de DataReportEntity
            DataReportEntity dataReportEntity = new DataReportEntity();
            dataReportEntity.setIdTool(toolId);
            dataReportEntity.setIdreport(savedReport.getId()); // Asignar el id del reporte guardado
            dataReportEntity.setNumber_of_times_borrowed(number_of_times_borrowed);

            // Agregar a la lista de DataReportEntity
            dataReportEntities.add(dataReportEntity);
        }

        // Guardar todos los DataReportEntity relacionados con el reporte
        dataReportRepository.saveAll(dataReportEntities);

        // Agregar el reporte creado a la lista de reportes
        createdReports.add(savedReport);

        return createdReports; // Retornar solo el reporte creado
    }




    public List<ReportEntity> ReportfilterDate(LocalDate initdate, LocalDate findate){
        List<ReportEntity> reports = reportRepository.findByDateBetweenOrderByDateDesc(initdate, findate);
        return reports;
    }

    public List<ReportEntity> ReportTopToolsAll(){
        List<ReportEntity> reports = reportRepository.findByName("ReportTopTools");
        return reports;
    }

    public List<ReportEntity> GetAllReportClientLoanLate(){
        List<ReportEntity> reports = reportRepository.findByName("ReportClientLoanLate");
        return reports;
    }

    public List<ReportEntity> GetAllReportLoanTools(){
        List<ReportEntity> reports = reportRepository.findByName("ReportLoanTools");
        return reports;
    }


}
