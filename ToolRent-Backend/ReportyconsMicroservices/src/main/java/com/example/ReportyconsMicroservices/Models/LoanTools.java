package com.example.ReportyconsMicroservices.Models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanTools {


    private Long id;

    private LocalDate initiallenddate;

    private LocalDate finalreturndate;

    private Long clientid;

    private Long toolid;

    private String status;

    private Double lateFee;

    private Double rentalFee;

    private Double damageFee;

    private Double repositionFee;

}
