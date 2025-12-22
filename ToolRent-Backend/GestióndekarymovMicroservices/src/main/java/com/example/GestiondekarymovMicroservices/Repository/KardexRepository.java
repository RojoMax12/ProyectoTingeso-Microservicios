package com.example.GestiondekarymovMicroservices.Repository;


import com.example.GestiondekarymovMicroservices.Entity.KardexEntity;
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

    @Query(value = """
    SELECT k.idtool, COUNT(*) as total 
    FROM kardex k 
    WHERE k.state_tools_id = 2 
    GROUP BY k.idtool 
    ORDER BY total DESC 
    LIMIT 5
        """, nativeQuery = true)
    List<Object[]> getTopToolIdsAndCounts();





}
