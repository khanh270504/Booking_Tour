package com.example.bookingtour.repositories;

import com.example.bookingtour.dtos.response.tour.TourSelectResponse;
import com.example.bookingtour.entities.Tour;
import com.example.bookingtour.enums.TourStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TourRepository extends JpaRepository<Tour, Integer>, JpaSpecificationExecutor<Tour> {
    List<Tour> findByStatus(TourStatus status);
    List<Tour> findByDestinationId(Integer destinationId);
    List<Tour> findByDestinationIdAndStatus(Integer destinationId, TourStatus status);

    @Query("SELECT new com.example.bookingtour.dtos.response.tour.TourSelectResponse(t.id, t.name) " +
            "FROM Tour t WHERE t.status = 'ACTIVE' ORDER BY t.id DESC")
    List<TourSelectResponse> getTourSelectList();
}