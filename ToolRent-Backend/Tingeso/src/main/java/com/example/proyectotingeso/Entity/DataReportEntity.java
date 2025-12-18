package com.example.proyectotingeso.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "DataReport")
@NoArgsConstructor
@AllArgsConstructor
public class DataReportEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(unique = true, nullable = false)
        private Long id;

        private Long idreport;

        @Column(nullable = true)
        private Long idLoanTool;

        @Column(nullable = true)
        private Long idClient;

        @Column(nullable = true)
        private Long idTool;

        @Column(nullable = true)
        private Long number_of_times_borrowed;


}
