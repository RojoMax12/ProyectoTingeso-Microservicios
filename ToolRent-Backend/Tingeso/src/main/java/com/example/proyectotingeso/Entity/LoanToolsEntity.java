package com.example.proyectotingeso.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "LoanTools")
@NoArgsConstructor
@AllArgsConstructor
public class LoanToolsEntity {
    //Prestamo
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private LocalDate initiallenddate;

    @Column(nullable = false)
    private LocalDate finalreturndate;

    @Column(nullable = false)
    private Long clientid;

    @Column(nullable = false)
    private Long toolid;

    private String status;

    private Double lateFee;
    
    private Double rentalFee;

    private Double damageFee;

    private Double repositionFee;

}
