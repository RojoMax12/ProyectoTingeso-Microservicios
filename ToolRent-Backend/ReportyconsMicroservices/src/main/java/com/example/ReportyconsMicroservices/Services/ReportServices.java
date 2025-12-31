package com.example.ReportyconsMicroservices.Services;


import com.example.ReportyconsMicroservices.Entity.DataReportEntity;
import com.example.ReportyconsMicroservices.Entity.ReportEntity;
import com.example.ReportyconsMicroservices.Models.Client;
import com.example.ReportyconsMicroservices.Models.Kardex;
import com.example.ReportyconsMicroservices.Models.LoanTools;
import com.example.ReportyconsMicroservices.Repository.DataReportRepository;
import com.example.ReportyconsMicroservices.Repository.ReportRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDate;
import java.util.*;


@Service
public class ReportServices {

    private static final Logger logger = LoggerFactory.getLogger(Kardex.class);

    @Autowired
    ReportRepository reportRepository;

    @Autowired
    DataReportRepository dataReportRepository;

    @Autowired
    RestTemplate restTemplate;


    public ReportEntity createReport(ReportEntity report) {
        return reportRepository.save(report);
    }

    @Transactional // 1. Garantiza que si falla el detalle, se borre el reporte principal (Rollback)
    public List<ReportEntity> ReportLoanTools() {
        LoanTools[] loantoolsArray;

        // 2. Manejo de errores de comunicación externa
        try {
            loantoolsArray = restTemplate.getForObject(
                    "http://m2-prestydev-service/api/LoanTools/Allloantoolstatusandrentalfee",
                    LoanTools[].class
            );
        } catch (RestClientException e) {
            logger.error("Error al conectar con m2-prestydev-service: {}", e.getMessage());
            // Puedes lanzar una excepción personalizada o devolver lista vacía según tu lógica
            return Collections.emptyList();
        }

        // 3. Validación de contenido
        if (loantoolsArray == null || loantoolsArray.length == 0) {
            logger.warn("No se encontraron préstamos para reportar.");
            return Collections.emptyList();
        }

        try {
            // 4. Persistencia del Reporte Principal
            ReportEntity reportEntity = new ReportEntity();
            reportEntity.setName("ReportLoanTools");
            reportEntity.setDate(LocalDate.now());

            ReportEntity savedReport = reportRepository.save(reportEntity);

            // 5. Mapeo de detalles (DataReportEntity)
            List<DataReportEntity> dataReportEntities = Arrays.stream(loantoolsArray)
                    .map(loan -> {
                        DataReportEntity detail = new DataReportEntity();
                        detail.setIdLoanTool(loan.getId());
                        detail.setIdreport(savedReport.getId());
                        return detail;
                    })
                    .toList();

            // 6. Guardado masivo
            dataReportRepository.saveAll(dataReportEntities);

            return List.of(savedReport);

        } catch (Exception e) {
            logger.error("Error interno al procesar el reporte: {}", e.getMessage());
            // Al estar en @Transactional, cualquier RuntimeException provocará Rollback
            throw new RuntimeException("Error al generar el reporte localmente", e);
        }
    }

    @Transactional // Crucial para evitar reportes sin detalles si falla la DB
    public List<ReportEntity> ReportClientLoanLate() {
        Client[] clientsArray;

        // 1. Manejo de error al llamar al microservicio m3-clientes
        try {
            clientsArray = restTemplate.getForObject(
                    "http://m3-clientes-service/api/Client/AllClientLoanLate",
                    Client[].class
            );
        } catch (RestClientException e) {
            logger.error("Error de comunicación con m3-clientes-service: {}", e.getMessage());
            return Collections.emptyList();
        }

        // 2. Validación de datos recibidos
        if (clientsArray == null || clientsArray.length == 0) {
            logger.info("No se encontraron clientes con préstamos atrasados.");
            return Collections.emptyList();
        }

        try {
            // 3. Crear el ReportEntity principal
            ReportEntity reportEntity = new ReportEntity();
            reportEntity.setName("ReportClientLoanLate");
            reportEntity.setDate(LocalDate.now());

            ReportEntity savedReport = reportRepository.save(reportEntity);

            // 4. Mapear los clientes a entidades de detalle
            List<DataReportEntity> dataReportEntities = Arrays.stream(clientsArray)
                    .map(client -> {
                        DataReportEntity detail = new DataReportEntity();
                        detail.setIdClient(client.getId()); // Usamos el ID del cliente del microservicio
                        detail.setIdreport(savedReport.getId());
                        return detail;
                    })
                    .toList();

            // 5. Guardar detalles
            dataReportRepository.saveAll(dataReportEntities);

            return List.of(savedReport);

        } catch (Exception e) {
            logger.error("Error al persistir el reporte de clientes morosos: {}", e.getMessage());
            // Lanza la excepción para que @Transactional haga el Rollback
            throw new RuntimeException("Fallo interno al procesar reporte de clientes", e);
        }
    }


    @Transactional
    public List<ReportEntity> createTopToolsReport() {
        Object[][] ranking;

        // 1. Manejo de errores de comunicación externa
        try {
            ranking = restTemplate.getForObject(
                    "https://m5-karymov-service/api/kardex/TopTool",
                    Object[][].class
            );
        } catch (RestClientException e) {
            logger.error("Error al obtener el ranking desde el servicio m5-karymov: {}", e.getMessage());
            return Collections.emptyList();
        }

        // 2. Validación de datos
        if (ranking == null || ranking.length == 0) {
            logger.info("El servicio de ranking devolvió una lista vacía.");
            return Collections.emptyList();
        }

        try {
            // 3. Crear el reporte principal
            ReportEntity reportEntity = new ReportEntity();
            reportEntity.setName("ReportTopTools");
            reportEntity.setDate(LocalDate.now());
            ReportEntity savedReport = reportRepository.save(reportEntity);

            List<DataReportEntity> dataReportEntities = new ArrayList<>();

            // 4. Procesar la matriz de objetos
            for (Object[] row : ranking) {
                try {
                    // Jackson suele deserializar números como Integer o Long. Number es el padre común.
                    Long toolId = ((Number) row[0]).longValue();
                    String toolName = (String) row[1];
                    Long timesBorrowed = ((Number) row[2]).longValue();

                    DataReportEntity detail = new DataReportEntity();
                    detail.setIdTool(toolId);
                    detail.setIdreport(savedReport.getId());
                    detail.setNumber_of_times_borrowed(timesBorrowed);

                    dataReportEntities.add(detail);

                    logger.debug("Procesado: ID {}, Herramienta: {}", toolId, toolName);

                } catch (Exception e) {
                    logger.warn("Error al procesar una fila del ranking: {}", e.getMessage());
                    // Continuamos con la siguiente fila si una está corrupta
                }
            }

            // 5. Persistencia masiva
            dataReportRepository.saveAll(dataReportEntities);

            return List.of(savedReport);

        } catch (Exception e) {
            logger.error("Error crítico al generar el reporte TopTools: {}", e.getMessage());
            // Provoca Rollback para no dejar un ReportEntity sin detalles
            throw new RuntimeException("Error interno en la generación del reporte", e);
        }
    }


    public List<ReportEntity> ReportfilterDate(LocalDate initdate, LocalDate findate) {
        List<ReportEntity> reports = reportRepository.findByDateBetweenOrderByDateDesc(initdate, findate);
        return reports;
    }

    public List<ReportEntity> ReportTopToolsAll() {
        List<ReportEntity> reports = reportRepository.findByName("ReportTopTools");
        return reports;
    }

    public List<ReportEntity> GetAllReportClientLoanLate() {
        List<ReportEntity> reports = reportRepository.findByName("ReportClientLoanLate");
        return reports;
    }

    public List<ReportEntity> GetAllReportLoanTools() {
        List<ReportEntity> reports = reportRepository.findByName("ReportLoanTools");
        return reports;
    }

    @Transactional
    public void deleteReportsById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            logger.warn("La lista de IDs para eliminar está vacía.");
            return;
        }

        try {
            // Spring Data JPA maneja esto internamente con una sola consulta eficiente
            reportRepository.deleteAllById(ids);

            logger.info("Se eliminaron correctamente {} reportes con IDs: {}", ids.size(), ids);
        } catch (Exception e) {
            logger.error("Error al eliminar los reportes por ID: {}", e.getMessage());
            // Importante lanzar la excepción para que @Transactional ejecute el Rollback
            throw new RuntimeException("Error en la base de datos al intentar borrar reportes", e);
        }
    }
}