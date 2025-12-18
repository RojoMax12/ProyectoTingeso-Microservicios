package com.example.GestiondePrestyDevMicroservices.Controllers;


import com.example.GestiondePrestyDevMicroservices.Entity.LoanToolsEntity;
import com.example.GestiondePrestyDevMicroservices.Services.LoanToolsServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/LoanTools")
@CrossOrigin("*")
public class LoanToolsController {

    @Autowired
    LoanToolsServices loanToolsServices;

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PostMapping("/")
    public ResponseEntity<LoanToolsEntity> createLoanTools(@RequestBody LoanToolsEntity loanToolsEntity) {
        LoanToolsEntity newLoanToos = loanToolsServices.CreateLoanToolsEntity(loanToolsEntity);
        return ResponseEntity.ok(newLoanToos);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/{id}")
    public ResponseEntity<LoanToolsEntity> getLoanTools(@PathVariable Long id) {
        LoanToolsEntity newLoanTools = loanToolsServices.getLoanToolsEntityById(id);
        return ResponseEntity.ok(newLoanTools);

    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PutMapping("/register-damage/{idloan}")
    public ResponseEntity<?> registerDamageandReposition(@PathVariable Long idloan){
        try {
            loanToolsServices.registerDamageFeeandReposition(idloan);
            return ResponseEntity.ok().build(); // Devuelve 200 OK si es exitoso (vacío)
        } catch (RuntimeException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PutMapping("/return/{iduser}/{idtools}")
    public ResponseEntity<LoanToolsEntity> returnLoanTools(@PathVariable Long iduser, @PathVariable Long idtools) {
        LoanToolsEntity newLoanTools = loanToolsServices.returnLoanTools(iduser, idtools);
        return ResponseEntity.ok(newLoanTools);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PutMapping("/")
    public ResponseEntity<LoanToolsEntity> updateLoanTools(@RequestBody LoanToolsEntity loanToolsEntity) {
        LoanToolsEntity newLoanTools = loanToolsServices.UpdateLoanToolsEntity(loanToolsEntity);
        return ResponseEntity.ok(newLoanTools);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteLoanTools(Long id) throws Exception {
        var isDeleted = loanToolsServices.DeleteLoanToolsEntity(id);
        return ResponseEntity.noContent().build();
    }
    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/userloantool/{id}")
    public ResponseEntity<List<LoanToolsEntity>> getLoanToolsByUserId(@PathVariable Long id) {
        List<LoanToolsEntity> usertool = loanToolsServices.getAlluserLoanTools(id);
        return ResponseEntity.ok(usertool);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/calculate-fine/{loanId}")
    public ResponseEntity<Double> calculateFineLoan(@PathVariable Long loanId) {
        double fine = loanToolsServices.calculateFine(loanId);
        return ResponseEntity.ok(fine);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/calculate-rental/{loanId}")
    public ResponseEntity<Double> calculateRentalLoan( @PathVariable Long loanId) {
        double rental = loanToolsServices.calculateRentalFee(loanId);
        return ResponseEntity.ok(rental);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PutMapping("/CheckClient/{idclient}")
    public ResponseEntity<Boolean> checkAndUpdateClientStatus(@PathVariable Long idclient) {
        boolean updated = loanToolsServices.checkAndUpdateClientStatus(idclient);
        if (updated) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PutMapping("/Pay/{idloan}")
    public ResponseEntity<Boolean> payloanfee(@PathVariable Long idloan){
        boolean updated = loanToolsServices.registerAllFeesPayment(idloan);
        if (updated) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }


}
