package com.example.bookingtour.Repositories;

import com.example.bookingtour.Entities.Tour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<Tour, Integer> {
    List<Tour> findByStatus(String status);
    List<Tour> findByDestinationId(Integer destinationId);
}