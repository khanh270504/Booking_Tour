package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.Booking;
import com.example.bookingtour.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    boolean existsByBooking(Booking booking);

    @Query("SELECT r FROM Review r WHERE r.tour.id = :tourId " +
            "AND (:rating IS NULL OR r.rating = :rating) " +
            "ORDER BY r.createdAt DESC")
    List<Review> findByTourWithFilter(@Param("tourId") Integer tourId, @Param("rating") Integer rating);
}