package com.example.GestiondekarymovMicroservices.Models;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tool {


    private Long id;

    private String name;

    private String category;

    private int replacement_cost;

    private Long states;
}

