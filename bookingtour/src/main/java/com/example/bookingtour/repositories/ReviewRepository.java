package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.Booking;
import com.example.bookingtour.entities.Review;
import com.example.bookingtour.enums.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    boolean existsByBooking(Booking booking);

    @Query("SELECT r FROM Review r WHERE r.tour.id = :tourId AND r.status = :status " +
            "AND (:rating IS NULL OR r.rating = :rating)")
    Page<Review> findByTourAndStatusWithFilter(
            @Param("tourId") Integer tourId,
            @Param("status") ReviewStatus status,
            @Param("rating") Integer rating,
            Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.tour.id = :tourId AND r.status = 'ACTIVE'")
    Double getAverageRatingByTourId(@Param("tourId") Integer tourId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.tour.id = :tourId AND r.status = 'ACTIVE'")
    Integer countActiveReviewsByTourId(@Param("tourId") Integer tourId);
}