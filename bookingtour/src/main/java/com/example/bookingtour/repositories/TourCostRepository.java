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
    @Query("SELECT EXTRACT(MONTH FROM t.createdAt), SUM(t.amount) " +
            "FROM TourCost t WHERE t.status != 'CANCELLED' AND EXTRACT(YEAR FROM t.createdAt) = EXTRACT(YEAR FROM CURRENT_DATE) " +
            "GROUP BY EXTRACT(MONTH FROM t.createdAt) ORDER BY EXTRACT(MONTH FROM t.createdAt)")
    List<Object[]> getCostByMonth();
    @Query("SELECT EXTRACT(DAY FROM t.createdAt), SUM(t.amount) " +
            "FROM TourCost t WHERE t.status != 'CANCELLED' " +
            "AND EXTRACT(MONTH FROM t.createdAt) = EXTRACT(MONTH FROM CURRENT_DATE) " +
            "AND EXTRACT(YEAR FROM t.createdAt) = EXTRACT(YEAR FROM CURRENT_DATE) " +
            "GROUP BY EXTRACT(DAY FROM t.createdAt) ORDER BY EXTRACT(DAY FROM t.createdAt)")
    List<Object[]> getCostByDayInCurrentMonth();
}