package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.ClientEntity;
import com.example.proyectotingeso.Services.ClientServices;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/Client")
@CrossOrigin("*")
public class ClientController {

    @Autowired
    ClientServices clientServices;

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PostMapping("/")
    ResponseEntity<ClientEntity> createClient(@RequestBody ClientEntity clientEntity){
        ClientEntity client = clientServices.createClient(clientEntity);
        return  ResponseEntity.ok(client);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/Allclient")
    ResponseEntity<List<ClientEntity>> getAllClient(){
        List<ClientEntity> clients = clientServices.getAllClients();
        return ResponseEntity.ok(clients);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/rut/{rut}")
    ResponseEntity<ClientEntity> getClientByRut(@PathVariable String rut){
        ClientEntity client = clientServices.getClientByRut(rut);
        return ResponseEntity.ok(client);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PutMapping("/UpdateClient")
    ResponseEntity<ClientEntity> updateClient(@RequestBody ClientEntity clientEntity){
        ClientEntity client = clientServices.updateClient(clientEntity);
        return ResponseEntity.ok(client);
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/Deleteclient/{idclient}")
    ResponseEntity<Boolean> deleteClientId(@PathVariable Long idclient) throws Exception{
        var isDelete = clientServices.deleteClient(idclient);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/{id}")
    ResponseEntity<ClientEntity> getClientById(@PathVariable Long id){
        ClientEntity client = clientServices.getClientById(id);
        return ResponseEntity.ok(client);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/AllClientLoanLate")
    ResponseEntity<List<ClientEntity>> getAllClientLoanLate(){
        List<ClientEntity> clients = clientServices.getAllClientLoanLate();
        return ResponseEntity.ok(clients);
    }



}
