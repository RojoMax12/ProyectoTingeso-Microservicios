package com.example.proyectotingeso.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "Kardex")
@NoArgsConstructor
@AllArgsConstructor
public class KardexEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    private Long StateToolsId;

    private LocalDate date;

    private String username;

    @Column(nullable = false)
    private Long idtool;

    private int quantity;

}
