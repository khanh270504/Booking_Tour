package com.example.bookingtour.dtos.response.booking;

import com.example.bookingtour.entities.Booking;
import com.example.bookingtour.entities.BookingPassenger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookingResponse {
    private Integer id;
    private String userEmail;
    private String customerFullName;

    private String tourName;
    private String departureDate;

    private BigDecimal totalOriginalPrice;
    private BigDecimal totalDiscount;
    private BigDecimal totalSurcharge;
    private BigDecimal totalFinalPrice;

    private String status;
    private String voucherCode;
    private Instant createdAt;

    private List<PassengerResponse> passengers;

    public static BookingResponse fromBooking(Booking booking, String customerFullName, List<BookingPassenger> passengers) {
        if (booking == null) {
            return null;
        }
        return BookingResponse.builder()
                .id(booking.getId())

                .userEmail(booking.getUser() != null ? booking.getUser().getEmail() : null)

                .customerFullName(customerFullName)

                .tourName(booking.getSchedule() != null && booking.getSchedule().getTour() != null
                        ? booking.getSchedule().getTour().getName() : null)

                .departureDate(booking.getSchedule() != null && booking.getSchedule().getDepartureDate() != null
                        ? booking.getSchedule().getDepartureDate().toString() : null)

                .totalOriginalPrice(booking.getTotalOriginalPrice())
                .totalDiscount(booking.getTotalDiscount())
                .totalSurcharge(booking.getTotalSurcharge())
                .totalFinalPrice(booking.getTotalFinalPrice())
                .status(booking.getStatus() != null ? booking.getStatus().name() : null)
                .voucherCode(booking.getVoucher() != null ? booking.getVoucher().getCode() : null)
                .createdAt(booking.getCreatedAt())

                .passengers(passengers != null
                        ? passengers.stream()
                        .map(PassengerResponse::fromPassenger)
                        .collect(Collectors.toList())
                        : null)
                .build();
    }
}