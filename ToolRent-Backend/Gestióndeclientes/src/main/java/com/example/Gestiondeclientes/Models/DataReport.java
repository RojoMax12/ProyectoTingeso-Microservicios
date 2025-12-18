package com.example.Gestiondeclientes.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
public class DataReport {

    private Long idreport;
    private Long idLoanTool;
    private Long idClient;
    private Long idTool;
    private Long number_of_times_borrowed;
}
