package com.example.bookingtour.Repositories;
import com.example.bookingtour.Entities.CrmLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface CrmLeadRepository extends JpaRepository<CrmLead, String> {
    List<CrmLead> findByAssignedStaffId(String staffId);
}