package com.example.proyectotingeso.Controllers;


import com.example.proyectotingeso.Entity.AmountsandratesEntity;
import com.example.proyectotingeso.Services.AmountsandratesServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/AmountandRates")
@CrossOrigin("*")
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



}
