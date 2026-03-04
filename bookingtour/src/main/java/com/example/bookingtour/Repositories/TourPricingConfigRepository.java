package com.example.bookingtour.Repositories;

import com.example.bookingtour.Entities.TourPricingConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TourPricingConfigRepository extends JpaRepository<TourPricingConfig, Integer> {
    @Query("SELECT p FROM TourPricingConfig p WHERE p.tour.id = :tourId AND p.passengerType = :type AND p.effectiveDate <= :departureDate ORDER BY p.effectiveDate DESC LIMIT 1")
    Optional<TourPricingConfig> findApplicablePrice(@Param("tourId") Integer tourId, @Param("type") String passengerType, @Param("departureDate") LocalDate departureDate);
}