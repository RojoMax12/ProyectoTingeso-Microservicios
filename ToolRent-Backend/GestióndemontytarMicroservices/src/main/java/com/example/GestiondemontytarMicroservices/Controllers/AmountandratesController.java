package com.example.GestiondemontytarMicroservices.Controllers;


import com.example.GestiondemontytarMicroservices.Entity.AmountsandratesEntity;
import com.example.GestiondemontytarMicroservices.Services.AmountsandratesServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/AmountandRates")
public class AmountandratesController {

    @Autowired
    AmountsandratesServices amountsandratesServices;

    @PreAuthorize(("hasAnyRole('ADMIN')"))
    @PostMapping("/")
    public ResponseEntity<AmountsandratesEntity> inizilizateAmounandRates(){
        AmountsandratesEntity amounsaanrate = amountsandratesServices.createAmountsAndRates();
        return ResponseEntity.ok(amounsaanrate);
    }

    @PreAuthorize(("hasAnyRole('ADMIN')"))
    @PutMapping("/update")
    public ResponseEntity<AmountsandratesEntity> updateamountandrate(@RequestBody AmountsandratesEntity amountsandratesEntity){
        AmountsandratesEntity newamout = amountsandratesServices.updateAmountAndRates(amountsandratesEntity);
        return ResponseEntity.ok(newamout);
    }

    @PreAuthorize(("hasAnyRole('ADMIN', 'USER')"))
    @GetMapping("/")
    public ResponseEntity<List<AmountsandratesEntity>> getAmountandrates(){
        List<AmountsandratesEntity> amountsandratesEntities = amountsandratesServices.getAllAmountsAndRates();
        return ResponseEntity.ok(amountsandratesEntities);
    }

    @PreAuthorize(("hasAnyRole('ADMIN', 'USER')"))
    @GetMapping("/{id}")
    public ResponseEntity<Optional<AmountsandratesEntity>> getAmountandratesById(@PathVariable Long id){
        Optional<AmountsandratesEntity> amountsandratesEntities = amountsandratesServices.getAmountsAndRatesbyId(id);
        return ResponseEntity.ok(amountsandratesEntities);
    }







}
