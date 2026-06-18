package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.TourSchedule;
import com.example.bookingtour.enums.ScheduleStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TourScheduleRepository extends JpaRepository<TourSchedule, Integer> {

    @Modifying
    @Query("UPDATE TourSchedule ts SET ts.availableSlots = ts.availableSlots - :slots WHERE ts.id = :scheduleId AND ts.availableSlots >= :slots")
    int subtractAvailableSlots(@Param("scheduleId") Integer scheduleId, @Param("slots") Integer slots);

    @Modifying
    @Query("UPDATE TourSchedule ts SET ts.availableSlots = ts.availableSlots + :slots WHERE ts.id = :scheduleId")
    void addAvailableSlots(@Param("scheduleId") Integer scheduleId, @Param("slots") Integer slots);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM TourSchedule s WHERE s.id = :id")
    Optional<TourSchedule> findByIdForUpdate(@Param("id") Integer id);

    List<TourSchedule> findByTourId(Integer tourId);

    boolean existsByTourIdAndStatus(Integer tourId, ScheduleStatus status);

    List<TourSchedule> findByDepartureDateLessThanEqualAndStatusIn(LocalDate date, List<ScheduleStatus> statuses);

    List<TourSchedule> findByReturnDateLessThanAndStatus(LocalDate date, ScheduleStatus status);

    @Query("SELECT s FROM TourSchedule s WHERE s.tour.id = :tourId " +
            "AND s.departureDate >= :today " +
            "AND s.status IN :statuses " +
            "ORDER BY s.departureDate ASC")
    List<TourSchedule> findValidSchedules(@Param("tourId") Integer tourId,
                                          @Param("today") LocalDate today,
                                          @Param("statuses") List<ScheduleStatus> statuses);
    List<TourSchedule> findByStatus(ScheduleStatus status);
    List<TourSchedule> findByTourIdIn(List<Integer> tourIds);
    Optional<TourSchedule> findFirstByTourIdAndStatus(Integer tourId, ScheduleStatus status);
}

