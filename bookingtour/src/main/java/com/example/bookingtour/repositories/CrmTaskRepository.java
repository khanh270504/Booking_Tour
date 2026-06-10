package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.CrmLead;
import com.example.bookingtour.entities.CrmTask;
import com.example.bookingtour.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CrmTaskRepository extends JpaRepository<CrmTask, Integer> {
    List<CrmTask> findByAssignedStaff_IdOrderByDueDateAsc(Integer staffId);
    boolean existsByLeadAndStatus(CrmLead lead, TaskStatus status);
    @Query("SELECT t FROM CrmTask t WHERE t.status = 'PENDING' AND t.dueDate < CURRENT_TIMESTAMP ORDER BY t.dueDate ASC LIMIT 5")
    List<CrmTask> findOverdueTasks();
    @Query("SELECT COUNT(t.id) FROM CrmTask t WHERE t.status = 'PENDING'")
    Integer countPendingTasks();
}
