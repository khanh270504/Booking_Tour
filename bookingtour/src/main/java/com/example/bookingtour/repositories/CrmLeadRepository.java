package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.CrmLead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrmLeadRepository extends JpaRepository<CrmLead, Integer> {

    @Query("SELECT c FROM CrmLead c WHERE c.assignedStaff.id = :staffId")
    List<CrmLead> getLeadsByStaffId(@Param("staffId") Integer staffId);

//    @Query("SELECT c FROM CrmLead c WHERE c.assignedStaff.user.userCode = :staffCode")
//    List<CrmLead> findByStaffCode(@Param("staffCode") String staffCode);
}