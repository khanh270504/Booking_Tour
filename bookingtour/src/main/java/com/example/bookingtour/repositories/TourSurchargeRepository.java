package com.example.bookingtour.repositories;
import com.example.bookingtour.entities.TourSurcharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface TourSurchargeRepository extends JpaRepository<TourSurcharge, Integer> {
    List<TourSurcharge> findByTourId(Integer tourId);
}