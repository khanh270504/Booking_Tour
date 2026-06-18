package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.Booking;
import com.example.bookingtour.entities.TourSchedule;
import com.example.bookingtour.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    List<Booking> findByContactEmailOrderByCreatedAtDesc(String email);
    List<Booking> findByCreatedBy_Id(Integer staffUserId);
    Optional<Booking> findByBookingCodeAndContactEmail(String bookingCode, String contactEmail);
    long countByVoucherIdAndContactEmail(Integer voucherId, String contactEmail);
    Optional<Booking> findByBookingCode(String bookingCode);
    List<Booking> findByScheduleId(Integer scheduleId);
    long countByCustomer_Id(Integer customerId);
    @Query("SELECT SUM(b.totalFinalPrice) FROM Booking b WHERE b.customer.id = :customerId")
    BigDecimal sumTotalPriceByCustomer_Id(@Param("customerId") Integer customerId);
    List<Booking> findByScheduleAndStatus(TourSchedule schedule, BookingStatus status);


    // Thống kê trạng thái booking (Status Chart)
    @Query("SELECT b.status, COUNT(b.id) FROM Booking b GROUP BY b.status")
    List<Object[]> countBookingsByStatus();

    @Query("SELECT EXTRACT(MONTH FROM b.createdAt), SUM(b.totalFinalPrice) " +
            "FROM Booking b " +
            "WHERE b.status IN (com.example.bookingtour.enums.BookingStatus.CONFIRMED, com.example.bookingtour.enums.BookingStatus.COMPLETED) " +
            "AND EXTRACT(YEAR FROM b.createdAt) = EXTRACT(YEAR FROM CURRENT_TIMESTAMP) " +
            "GROUP BY EXTRACT(MONTH FROM b.createdAt) " +
            "ORDER BY EXTRACT(MONTH FROM b.createdAt)")
    List<Object[]> getRevenueByMonth();
    @Query("SELECT EXTRACT(DAY FROM b.createdAt), SUM(b.totalFinalPrice) " +
            "FROM Booking b " +
            "WHERE b.status IN (com.example.bookingtour.enums.BookingStatus.CONFIRMED, com.example.bookingtour.enums.BookingStatus.COMPLETED) " +
            "AND EXTRACT(MONTH FROM b.createdAt) = EXTRACT(MONTH FROM CURRENT_TIMESTAMP) " +
            "AND EXTRACT(YEAR FROM b.createdAt) = EXTRACT(YEAR FROM CURRENT_TIMESTAMP) " +
            "GROUP BY EXTRACT(DAY FROM b.createdAt) " +
            "ORDER BY EXTRACT(DAY FROM b.createdAt)")
    List<Object[]> getRevenueByDayInCurrentMonth();

    @Query("SELECT d.name, COUNT(b.id) " +
            "FROM Booking b " +
            "JOIN b.schedule s " +
            "JOIN s.tour t " +
            "JOIN t.destination d " +
            "GROUP BY d.name " +
            "ORDER BY COUNT(b.id) DESC")
    List<Object[]> getTopDestinations();

    @Query("SELECT sp.fullName, SUM(b.totalFinalPrice), COUNT(b.id) " +
            "FROM Booking b " +
            "JOIN b.createdBy u " +
            "JOIN StaffProfile sp ON sp.id = u.id " +
            "WHERE b.status IN (com.example.bookingtour.enums.BookingStatus.CONFIRMED, com.example.bookingtour.enums.BookingStatus.COMPLETED) " +
            "AND EXTRACT(YEAR FROM b.createdAt) = EXTRACT(YEAR FROM CURRENT_TIMESTAMP) " +
            "GROUP BY sp.id, sp.fullName " +
            "ORDER BY SUM(b.totalFinalPrice) DESC")
    List<Object[]> getSalesLeaderboard();

}