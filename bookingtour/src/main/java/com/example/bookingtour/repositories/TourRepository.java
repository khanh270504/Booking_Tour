package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.Tour;
import com.example.bookingtour.enums.TourStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<Tour, Integer>, JpaSpecificationExecutor<Tour> {
    List<Tour> findByStatus(TourStatus status);
    List<Tour> findByDestinationId(Integer destinationId);
    List<Tour> findByDestinationIdAndStatus(Integer destinationId, TourStatus status);
}