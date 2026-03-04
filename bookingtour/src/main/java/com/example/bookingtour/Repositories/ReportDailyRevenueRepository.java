package com.example.bookingtour.Repositories;
import com.example.bookingtour.Entities.ReportDailyRevenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
@Repository
public interface ReportDailyRevenueRepository extends JpaRepository<ReportDailyRevenue, LocalDate> {}