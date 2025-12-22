package com.example.ReportyconsMicroservices.Services;


import com.example.ReportyconsMicroservices.Entity.DataReportEntity;
import com.example.ReportyconsMicroservices.Entity.ReportEntity;
import com.example.ReportyconsMicroservices.Models.Client;
import com.example.ReportyconsMicroservices.Models.LoanTools;
import com.example.ReportyconsMicroservices.Repository.DataReportRepository;
import com.example.ReportyconsMicroservices.Repository.ReportRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDate;
import java.util.*;


@Service
public class ReportServices {

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    DataReportRepository dataReportRepository;

    @Autowired
    RestTemplate restTemplate;


    public ReportEntity createReport(ReportEntity report) {
        return reportRepository.save(report);
    }

    public List<ReportEntity> ReportLoanTools() {
        // 1. CAMBIO CRÍTICO: Usar LoanTools[].class en lugar de List.class
        // Esto evita que Jackson convierta los datos en LinkedHashMap
        LoanTools[] loantoolsArray = restTemplate.getForObject(
                "http://GESTIONDEPRESTYDEVMICROSERVICES/api/LoanTools/Allloantoolstatusandrentalfee",
                LoanTools[].class
        );

        // Verificación de seguridad
        if (loantoolsArray == null) {
            return new ArrayList<>();
        }

        System.out.println("Préstamos encontrados: " + loantoolsArray.length);
        LocalDate date = LocalDate.now();

        // 2. Crear y guardar el reporte principal
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setName("ReportLoanTools");
        reportEntity.setDate(date);

        // Si IntelliJ te marca error aquí, recuerda hacer el cast: (ReportEntity)
        ReportEntity savedReport = reportRepository.save(reportEntity);

        // 3. Crear los detalles del reporte
        List<DataReportEntity> dataReportEntities = new ArrayList<>();

        // Al ser un Array, el for-each ahora funciona perfectamente con el tipo LoanTools
        for (LoanTools loan : loantoolsArray) {
            DataReportEntity dataReportEntity = new DataReportEntity();
            dataReportEntity.setIdLoanTool(loan.getId());
            dataReportEntity.setIdreport(savedReport.getId());

            dataReportEntities.add(dataReportEntity);
        }

        // 4. Persistir los datos y retornar
        dataReportRepository.saveAll(dataReportEntities);

        return List.of(savedReport);
    }

    public List<ReportEntity> ReportClientLoanLate() {
        // 1. CAMBIO CLAVE: Usar el Array Client[].class para que Jackson mapee correctamente los objetos
        Client[] clientsArray = restTemplate.getForObject(
                "http://GESTIONDECLIENTES/api/Client/AllClientLoanLate",
                Client[].class
        );

        // Verificación de nulidad por seguridad
        if (clientsArray == null) {
            return new ArrayList<>();
        }

        // Crear el ReportEntity principal
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setName("ReportClientLoanLate");
        reportEntity.setDate(LocalDate.now());

        // Guardar el reporte (asegúrate de que el repositorio devuelva ReportEntity)
        ReportEntity savedReport = reportRepository.save(reportEntity);

        // Lista para almacenar los detalles del reporte
        List<DataReportEntity> dataReportEntities = new ArrayList<>();

        // 2. Iterar sobre el Array. Ahora 'client' será reconocido como tipo Client y no como un Map.
        for (Client client : clientsArray) {
            DataReportEntity dataReportEntity = new DataReportEntity();
            dataReportEntity.setIdClient(client.getId());
            dataReportEntity.setIdreport(savedReport.getId());

            dataReportEntities.add(dataReportEntity);
        }

        // Guardar todos los detalles en la base de datos
        dataReportRepository.saveAll(dataReportEntities);

        return List.of(savedReport);
    }


    public List<ReportEntity> createTopToolsReport() {
        // 1. CAMBIO FUNDAMENTAL: Usar Object[][].class en lugar de List.class
        Object[][] ranking = restTemplate.getForObject(
                "https://GESTIONDEKARYMOVMICROSERVICES/api/kardex/TopTool",
                Object[][].class
        );

        if (ranking == null || ranking.length == 0) {
            return new ArrayList<>();
        }

        // Crear el reporte maestro
        ReportEntity reportEntity = new ReportEntity();
        reportEntity.setName("ReportTopTools");
        reportEntity.setDate(LocalDate.now());
        ReportEntity savedReport = reportRepository.save(reportEntity);

        List<DataReportEntity> dataReportEntities = new ArrayList<>();

        // 2. Iterar sobre el array de arrays
        for (Object[] row : ranking) {
            // 3. USO DE Number: Jackson puede traer números como Integer o Double.
            // Castear a Number y luego usar .longValue() es lo más seguro.
            Long toolId = ((Number) row[0]).longValue();
            String toolName = (String) row[1];
            Long timesBorrowed = ((Number) row[2]).longValue();

            // Imprimir para debug
            System.out.println(String.format("ID: %d, Herramienta: %s, Veces Prestada: %d", toolId, toolName, timesBorrowed));

            DataReportEntity dataReportEntity = new DataReportEntity();
            dataReportEntity.setIdTool(toolId);
            dataReportEntity.setIdreport(savedReport.getId());
            dataReportEntity.setNumber_of_times_borrowed(timesBorrowed);

            dataReportEntities.add(dataReportEntity);
        }

        dataReportRepository.saveAll(dataReportEntities);

        List<ReportEntity> createdReports = new ArrayList<>();
        createdReports.add(savedReport);
        return createdReports;
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
