package com.example.bookingtour.Repositories;
import com.example.bookingtour.Entities.TourCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface TourCostRepository extends JpaRepository<TourCost, Integer> {
    List<TourCost> findByScheduleId(Integer scheduleId);
}