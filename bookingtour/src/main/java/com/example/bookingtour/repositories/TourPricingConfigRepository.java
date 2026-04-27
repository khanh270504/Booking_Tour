package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.TourPricingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TourPricingConfigRepository extends JpaRepository<TourPricingConfig, Integer> {


    List<TourPricingConfig> findByScheduleId(Integer tourId);

}