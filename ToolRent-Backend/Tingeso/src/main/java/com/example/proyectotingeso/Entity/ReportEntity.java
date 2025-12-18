package com.example.proyectotingeso.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "Reports")
@NoArgsConstructor
@AllArgsConstructor
    public class ReportEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(unique = true, nullable = false)
        private Long id;

        private String name;

        private LocalDate date;

}
