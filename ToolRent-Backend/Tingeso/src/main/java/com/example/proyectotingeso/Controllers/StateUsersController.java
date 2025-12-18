package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.StateUsersEntity;
import com.example.proyectotingeso.Services.StateUsersServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stateuser")
@CrossOrigin("*")
public class StateUsersController {

    @Autowired
    private StateUsersServices stateUsersServices;

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PostMapping("/")
    public ResponseEntity<String> createStateUser() {
        String mensage = stateUsersServices.CreateStateUsers();
        return ResponseEntity.ok(mensage);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/{id}")
    public ResponseEntity<StateUsersEntity> getStateUser(@PathVariable Long id) {
        StateUsersEntity newstateuser = stateUsersServices.getStateUsersById(id);
        return ResponseEntity.ok(newstateuser);
    }
    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/")
    public ResponseEntity<List<StateUsersEntity>> getAllStateUsers() {
        List<StateUsersEntity> newstateusers = stateUsersServices.getAllStateUsers();
        return ResponseEntity.ok(newstateusers);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @DeleteMapping("/{id}")
    public ResponseEntity<StateUsersEntity> deleteStateUser(@PathVariable Long id) throws Exception {
        var delete = stateUsersServices.deleteStateUsersById(id);
        return ResponseEntity.noContent().build();

    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PutMapping("/UpdateStateUsers")
    public ResponseEntity<StateUsersEntity> updateStateUsers(@RequestBody StateUsersEntity stateUsersEntity) {
        StateUsersEntity newstateuser = stateUsersServices.updateStateUsers(stateUsersEntity);
        return ResponseEntity.ok(newstateuser);
    }

}
