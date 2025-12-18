package com.example.proyectotingeso.Controllers;


import com.example.proyectotingeso.Entity.ReportEntity;
import com.example.proyectotingeso.Services.ReportServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/report")
@CrossOrigin("*")
public class ReportController {

    @Autowired
    ReportServices reportServices;

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PostMapping("/ReportLoan")
    public ResponseEntity<List<ReportEntity>> createreportloan(){
        List<ReportEntity> reports = reportServices.ReportLoanTools();
        return ResponseEntity.ok(reports);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PostMapping("/ReportClientLate")
    public ResponseEntity<List<ReportEntity>> createreportclientlate(){
        List<ReportEntity> reports = reportServices.ReportClientLoanLate();
        return ResponseEntity.ok(reports);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PostMapping("/ReportTopTools")
    public ResponseEntity<List<ReportEntity>> createreporttoptools(){
        List<ReportEntity> reports = reportServices.createTopToolsReport();
        return ResponseEntity.ok(reports);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/Reports/{initdate}/{findate}")
    public ResponseEntity<List<ReportEntity>> getreportbydate(@PathVariable LocalDate initdate, @PathVariable LocalDate findate){
        List<ReportEntity> reports = reportServices.ReportfilterDate(initdate,findate);
        return ResponseEntity.ok(reports);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/AllReports")
    public ResponseEntity<List<ReportEntity>> getreport(){
        List<ReportEntity> reports = reportServices.ReportLoanTools();
        return ResponseEntity.ok(reports);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/AllReportsLoan")
    public ResponseEntity<List<ReportEntity>> getreportloan(){
        List<ReportEntity> reports = reportServices.GetAllReportLoanTools();
        return ResponseEntity.ok(reports);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/AllReportClientLate")
    public ResponseEntity<List<ReportEntity>> getreportclientlate(){
        List<ReportEntity> reports = reportServices.GetAllReportClientLoanLate();
        return ResponseEntity.ok(reports);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/AllReportTopTool")
    public ResponseEntity<List<ReportEntity>> getreporttoptool(){
        List<ReportEntity> reports = reportServices.ReportTopToolsAll();
        return ResponseEntity.ok(reports);
    }



}
