package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.StateToolsEntity;
import com.example.proyectotingeso.Services.StateToolsServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/statetools")
@CrossOrigin("*")
public class StateToolsController {

    @Autowired
    StateToolsServices stateToolsServices;

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PostMapping("/")
    public ResponseEntity<String> createStateTools() {
        String menssage = stateToolsServices.createStateTools();
        return ResponseEntity.ok(menssage);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/{id}")
    public ResponseEntity<StateToolsEntity> getStateToolsEntityById(@PathVariable Long id) {
        StateToolsEntity stateToolsEntity = stateToolsServices.getStateToolsEntityById(id);
        return ResponseEntity.ok(stateToolsEntity);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PutMapping("/")
    public ResponseEntity<StateToolsEntity> updateStateToolsEntity(@RequestBody StateToolsEntity stateToolsEntity) {
        StateToolsEntity newStateToolsEntity = stateToolsServices.updateStateToolsEntity(stateToolsEntity);
        return ResponseEntity.ok(newStateToolsEntity);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteStateToolsEntityById(@PathVariable Long id) throws Exception {
        var isDelete = stateToolsServices.deleteStateToolsById(id);
        return ResponseEntity.noContent().build();
    }
}
