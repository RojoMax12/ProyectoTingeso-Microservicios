package com.example.ReportyconsMicroservices.Models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    private Long id;

    private String name;

    private String email;

    private String rut;

    private String phone;

    private Long state;
}
