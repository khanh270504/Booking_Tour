package com.example.bookingtour.Repositories;
import com.example.bookingtour.Entities.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer> {}