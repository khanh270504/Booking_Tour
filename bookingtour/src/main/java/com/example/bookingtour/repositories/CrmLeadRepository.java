package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.CrmLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrmLeadRepository extends JpaRepository<CrmLead, Integer> {

    List<CrmLead> findByAssignedStaff_IdOrderByCreatedAtDesc(Integer staffId);
    List<CrmLead> findAllByOrderByCreatedAtDesc();

    @Query("SELECT l.status, COUNT(l.id) FROM CrmLead l GROUP BY l.status ORDER BY l.status ASC")
    List<Object[]> getLeadFunnel();

    @Query("SELECT l.source, COUNT(l.id) FROM CrmLead l GROUP BY l.source")
    List<Object[]> getLeadSources();
}