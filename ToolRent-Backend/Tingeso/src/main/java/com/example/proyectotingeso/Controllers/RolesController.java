package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.RoleEntity;
import com.example.proyectotingeso.Services.RoleServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
@CrossOrigin("*")
public class RolesController {

    @Autowired
    RoleServices roleServices;

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PostMapping("/")
    public ResponseEntity<String> createRole() {
        String mensage = roleServices.createRole();
        return ResponseEntity.ok(mensage);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/Allroles")
    public ResponseEntity<List<RoleEntity>> getAllRoles() {
        List<RoleEntity> roles = roleServices.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/{id}")
    public ResponseEntity<RoleEntity> getRoleById(@PathVariable Long id) {
        RoleEntity newRole = roleServices.getRoleById(id).get();
        return ResponseEntity.ok(newRole);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @DeleteMapping("/{id}")
    public ResponseEntity<Boolean> deleteRole(@PathVariable Long id) throws Exception {
        var isDeleted = roleServices.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PutMapping("/")
    public ResponseEntity<RoleEntity> updateRole(@RequestBody RoleEntity role) {
        RoleEntity newRole = roleServices.updateRole(role);
        return ResponseEntity.ok(newRole);
    }
}
