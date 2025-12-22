package com.example.ReportyconsMicroservices.Models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Kardex {

    private Long id;

    private Long StateToolsId;

    private LocalDate date;

    private String username;

    private Long idtool;

    private int quantity;
}
