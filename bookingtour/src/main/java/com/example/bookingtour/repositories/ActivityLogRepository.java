package com.example.bookingtour.repositories;
import com.example.bookingtour.entities.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Integer> {}