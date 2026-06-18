package com.example.bookingtour.repositories;
import com.example.bookingtour.entities.TourCost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface TourCostRepository extends JpaRepository<TourCost, Integer> {
    List<TourCost> findByScheduleId(Integer scheduleId);
    List<TourCost> findByProviderId(Integer providerId);
    Page<TourCost> findByExpenseNameContainingIgnoreCase(String keyword, Pageable pageable);
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TourCost t WHERE t.status != 'CANCELLED'")
    Double calculateTotalValidAmount();

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM TourCost t WHERE t.status = 'UNPAID'")
    Double calculateTotalUnpaidAmount();
    @Query("SELECT EXTRACT(MONTH FROM s.departureDate), SUM(t.amount) " +
            "FROM TourCost t " +
            "JOIN t.schedule s " +
            "WHERE t.status != com.example.bookingtour.enums.TourCostStatus.CANCELLED " + // Gọi chuẩn Enum bảo mật
            "AND EXTRACT(YEAR FROM s.departureDate) = EXTRACT(YEAR FROM CURRENT_TIMESTAMP) " +
            "GROUP BY EXTRACT(MONTH FROM s.departureDate) " +
            "ORDER BY EXTRACT(MONTH FROM s.departureDate)")
    List<Object[]> getCostByMonth();
    @Query("SELECT EXTRACT(DAY FROM s.departureDate), SUM(t.amount) " +
            "FROM TourCost t " +
            "JOIN t.schedule s " +
            "WHERE t.status != com.example.bookingtour.enums.TourCostStatus.CANCELLED " +
            "AND EXTRACT(MONTH FROM s.departureDate) = EXTRACT(MONTH FROM CURRENT_TIMESTAMP) " +
            "AND EXTRACT(YEAR FROM s.departureDate) = EXTRACT(YEAR FROM CURRENT_TIMESTAMP) " +
            "GROUP BY EXTRACT(DAY FROM s.departureDate) " +
            "ORDER BY EXTRACT(DAY FROM s.departureDate)")
    List<Object[]> getCostByDayInCurrentMonth();
}