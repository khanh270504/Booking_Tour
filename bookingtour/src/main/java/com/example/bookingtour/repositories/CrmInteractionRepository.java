package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.CrmInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrmInteractionRepository extends JpaRepository<CrmInteraction, Integer> {

    List<CrmInteraction> findByLeadIdOrderByCreatedAtDesc(Integer leadId);

}