package com.example.bookingtour.repositories;
import com.example.bookingtour.entities.ReportDailyRevenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
@Repository
public interface ReportDailyRevenueRepository extends JpaRepository<ReportDailyRevenue, LocalDate> {}