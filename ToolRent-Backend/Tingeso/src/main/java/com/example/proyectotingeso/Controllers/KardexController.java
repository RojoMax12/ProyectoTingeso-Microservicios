package com.example.proyectotingeso.Controllers;

import com.example.proyectotingeso.Entity.KardexEntity;
import com.example.proyectotingeso.Services.KardexServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/kardex")
@CrossOrigin("*")
public class KardexController {

    @Autowired
    KardexServices kardexServices;

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PostMapping("/")
    public ResponseEntity<KardexEntity> createKardex(@RequestBody KardexEntity kardexEntity) {
        KardexEntity newKardexEntity = kardexServices.save(kardexEntity);
        return ResponseEntity.ok(newKardexEntity);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @PutMapping("/update")
    public ResponseEntity<KardexEntity> updateKardex(@RequestBody KardexEntity kardexEntity) {
        KardexEntity newKardexEntity = kardexServices.Update(kardexEntity);
        return ResponseEntity.ok(newKardexEntity);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/Allkardex")
    public ResponseEntity<List<KardexEntity>> getAllKardex() {
        List<KardexEntity> kardex = kardexServices.findAll();
        return ResponseEntity.ok(kardex);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteKardex(@PathVariable Long id) throws Exception {
        var isDelete = kardexServices.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/Range/{dateinit}/{datefin}")
    public ResponseEntity<List<KardexEntity>> getrangeKardex(@PathVariable LocalDate dateinit, @PathVariable LocalDate datefin) {
        List<KardexEntity> listkardex = kardexServices.HistoryKardexDateInitandDateFin(dateinit, datefin);
        return ResponseEntity.ok(listkardex);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/History/{nametool}")
    public ResponseEntity<List<KardexEntity>> gethistorynametoolKardex(@PathVariable String nametool){
        List<KardexEntity> listkardex = kardexServices.HistoryKardexTool(nametool);
        return ResponseEntity.ok(listkardex);
    }

    @PreAuthorize(("hasAnyRole('USER','ADMIN')"))
    @GetMapping("/TopTool")
    public ResponseEntity<List<Object[]>> getToptoolKardex(){
        List<Object[]> toptool = kardexServices.TopToolKardexTool();
        return ResponseEntity.ok(toptool);
    }

}
