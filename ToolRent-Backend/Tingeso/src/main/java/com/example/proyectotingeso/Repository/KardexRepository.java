package com.example.proyectotingeso.Repository;

import com.example.proyectotingeso.Entity.KardexEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface KardexRepository extends JpaRepository<KardexEntity, Long> {

    public Optional<KardexEntity> findById(Long id);

    public List<KardexEntity> findAllByIdtool(Long idTool);

    public List<KardexEntity> findByDateBetweenOrderByDateDesc(LocalDate init, LocalDate fin);

    @Query("""
        SELECT 
               t.id AS idTool,
               t.name AS name,
               COUNT(k) AS totalPrestamos
        FROM KardexEntity k
        JOIN ToolEntity t ON t.id = k.idtool
        WHERE k.StateToolsId = 2
        GROUP BY t.id, t.name
        ORDER BY totalPrestamos DESC
        LIMIT 5
    """)
    List<Object[]> getTopTools();





}
