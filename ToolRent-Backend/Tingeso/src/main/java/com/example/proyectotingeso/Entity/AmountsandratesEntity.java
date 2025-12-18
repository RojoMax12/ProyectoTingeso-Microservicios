package com.example.proyectotingeso.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Table(name = "Amountsandrates")
@NoArgsConstructor
@AllArgsConstructor
public class AmountsandratesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private Long id;

    private double dailyrentalrate;

    private double dailylatefeefine;

    private double reparationcharge;

}
