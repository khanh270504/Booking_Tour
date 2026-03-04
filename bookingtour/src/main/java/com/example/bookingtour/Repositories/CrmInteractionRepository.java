package com.example.bookingtour.Repositories;
import com.example.bookingtour.Entities.CrmInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface CrmInteractionRepository extends JpaRepository<CrmInteraction, Integer> {
    List<CrmInteraction> findByLeadIdOrderByCreatedAtDesc(String leadId);
}