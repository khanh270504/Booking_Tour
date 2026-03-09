package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.TourSchedule;
import com.example.bookingtour.enums.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, Integer> {

    @Modifying
    @Query("UPDATE TourSchedule ts SET ts.availableSlots = ts.availableSlots - :slots WHERE ts.id = :scheduleId AND ts.availableSlots >= :slots")
    int subtractAvailableSlots(@Param("scheduleId") Integer scheduleId, @Param("slots") Integer slots);

    @Modifying
    @Query("UPDATE TourSchedule ts SET ts.availableSlots = ts.availableSlots + :slots WHERE ts.id = :scheduleId")
    void addAvailableSlots(@Param("scheduleId") Integer scheduleId, @Param("slots") Integer slots);


    List<TourSchedule> findByTourId(Integer tourId);

    boolean existsByTourIdAndStatus(Integer tourId, ScheduleStatus status);
}