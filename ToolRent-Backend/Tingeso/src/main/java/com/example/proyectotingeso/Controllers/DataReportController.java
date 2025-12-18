package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.DataReportEntity;
import com.example.proyectotingeso.Services.DataReportServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/DataReport")
@CrossOrigin("*")
public class DataReportController {

    @Autowired
    DataReportServices dataReportService;

    // Crear DataReportEntity
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/")
    public ResponseEntity<DataReportEntity> createDataReport(@RequestBody DataReportEntity dataReportEntity) {
        DataReportEntity savedDataReport = dataReportService.createDataReport(dataReportEntity);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDataReport);
    }

    // Obtener DataReportEntity por IDreport
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<List<DataReportEntity>> getDataReportByIdreport(@PathVariable Long id) {
        List<DataReportEntity> dataReport = dataReportService.findReportByIdreport(id);
        return ResponseEntity.status(HttpStatus.OK).body(dataReport);
    }

    // Obtener todos los DataReportEntity
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<DataReportEntity>> getAllDataReports() {
        List<DataReportEntity> dataReports = dataReportService.findAllDataReport();
        return ResponseEntity.ok(dataReports);
    }

    // Verificar si existe DataReportEntity por ID de Cliente
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/existsByClient/{idClient}")
    public ResponseEntity<Boolean> existsByIdClient(@PathVariable Long idClient) {
        Boolean exists = dataReportService.existsByIdClient(idClient);
        return ResponseEntity.ok(exists);
    }

    // Verificar si existe DataReportEntity por ID de LoanTool
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping("/existsByLoanTool/{idLoanTool}")
    public ResponseEntity<Boolean> existsByIdLoanTool(@PathVariable Long idLoanTool) {
        Boolean exists = dataReportService.existsByIdLoanTool(idLoanTool);
        return ResponseEntity.ok(exists);
    }
}
