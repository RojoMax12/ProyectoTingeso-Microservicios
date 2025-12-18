package com.example.Gestiondeclientes.Models;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanTools {

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
