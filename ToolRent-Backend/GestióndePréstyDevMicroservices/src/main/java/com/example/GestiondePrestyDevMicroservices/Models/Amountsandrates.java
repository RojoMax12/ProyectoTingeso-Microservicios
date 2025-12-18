package com.example.GestiondePrestyDevMicroservices.Models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Amountsandrates {

    private Long id;

    private double dailyrentalrate;

    private double dailylatefeefine;

    private double reparationcharge;

}
