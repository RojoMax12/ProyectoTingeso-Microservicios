package com.example.proyectotingeso.Repository;

import com.example.proyectotingeso.Entity.LoanToolsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanToolsRepository extends JpaRepository<LoanToolsEntity, Long> {

    public Optional<LoanToolsEntity> findById(Long id);

    public Optional<LoanToolsEntity> findByClientidAndToolid(Long clientid, Long toolid);

    public List<LoanToolsEntity> findAllByClientid(Long clientid);

    public List<LoanToolsEntity> findAllBystatusInAndRentalFeeGreaterThan(Collection<String> statuses, double rentalFee);

    public List<LoanToolsEntity> findAllBystatus(String status);

    public  List<LoanToolsEntity> findAllByClientidAndStatus(Long clientid, String status);


}
