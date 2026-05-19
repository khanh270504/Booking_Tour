package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.CrmLead;
import com.example.bookingtour.entities.CrmTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrmTaskRepository extends JpaRepository<CrmTask, Integer> {
    List<CrmTask> findByAssignedStaff_IdOrderByDueDateAsc(Integer staffId);
}
