package com.example.bookingtour.Repositories;

import com.example.bookingtour.Entities.TourSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, Integer> {

    @Modifying
    @Query("UPDATE TourSchedule ts SET ts.availableSlots = ts.availableSlots - :slots WHERE ts.id = :scheduleId AND ts.availableSlots >= :slots")
    int subtractAvailableSlots(@Param("scheduleId") Integer scheduleId, @Param("slots") Integer slots);

    @Modifying
    @Query("UPDATE TourSchedule ts SET ts.availableSlots = ts.availableSlots + :slots WHERE ts.id = :scheduleId")
    void addAvailableSlots(@Param("scheduleId") Integer scheduleId, @Param("slots") Integer slots);
}