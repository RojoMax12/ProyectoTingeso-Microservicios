package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.ToolEntity;
import com.example.proyectotingeso.Entity.UserEntity;
import com.example.proyectotingeso.Services.ToolServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/Tools")
@CrossOrigin("*")
public class ToolController {

    @Autowired
    ToolServices toolServices;

    //- Usar @PreAuthorize("hasAnyRole('USER')") según corresponda en cada endpoint.
    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PostMapping("/")
    public ResponseEntity<ToolEntity> addtool(@RequestBody ToolEntity toolEntity) {
        ToolEntity newToolsEntity = toolServices.save(toolEntity);
        return ResponseEntity.ok(newToolsEntity);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/alltools")
    public ResponseEntity<List<ToolEntity>> getAlltools() {
        List<ToolEntity> tools = toolServices.getAlltool();
        return ResponseEntity.ok(tools);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/tool/{id}")
    public ResponseEntity<ToolEntity> gettool(@PathVariable Long id) {
        ToolEntity tool = toolServices.getTool(id);
        return ResponseEntity.ok(tool);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PutMapping("/UpdateTool")
    public ResponseEntity<ToolEntity> updatetool(@RequestBody ToolEntity toolEntity) {
        ToolEntity newtoolEntity = toolServices.updateTool(toolEntity);
        return ResponseEntity.ok(newtoolEntity);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletetool(@PathVariable Long id) throws Exception {
        var isDelete = toolServices.deletetoolbyid(id);
        return ResponseEntity.noContent().build();

    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/inventory")
    public ResponseEntity<Integer> getinvetory(@RequestBody ToolEntity toolEntity) {
        int inv = toolServices.inventory(toolEntity);
        return ResponseEntity.ok(inv);
    }

    @PreAuthorize(("hasAnyRole('ADMIN')"))
    @PutMapping("/{idtool}")
    public ResponseEntity<ToolEntity> unsubscribeTool(@PathVariable Long idtool) throws Exception {
        ToolEntity tool = toolServices.unsubscribeToolAdmin(idtool);
        return ResponseEntity.ok(tool);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PutMapping("/Borrowed/{idtool}")
    public ResponseEntity<ToolEntity> borrowedTool(@PathVariable Long idtool) throws Exception{
        ToolEntity tool = toolServices.borrowedTool(idtool);
        return ResponseEntity.ok(tool);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PutMapping("/inrepair/{idtool}")
    public ResponseEntity<ToolEntity> inrepairTool(@PathVariable Long idtool) throws Exception{
        ToolEntity tool = toolServices.inrepair(idtool);
        return ResponseEntity.ok(tool);
    }
}
