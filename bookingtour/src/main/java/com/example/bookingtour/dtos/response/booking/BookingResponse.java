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

    private String bookingCode;

    private String contactName;
    private String contactPhone;
    private String contactEmail;

    private String tourName;
    private String departureDate;
    private String departureLocation;

    private String note;

    private BigDecimal totalOriginalPrice;
    private BigDecimal totalDiscount;
    private BigDecimal totalSurcharge;
    private BigDecimal totalFinalPrice;

    private String status;
    private String voucherCode;
    private Instant createdAt;

    private String createdByEmail;

    private List<PassengerResponse> passengers;

    public static BookingResponse fromBooking(Booking booking, List<BookingPassenger> passengers) {
        if (booking == null) return null;

        return BookingResponse.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())

                .contactName(booking.getContactName())
                .contactPhone(booking.getContactPhone())
                .contactEmail(booking.getContactEmail())

                .tourName(booking.getTourNameSnapshot())
                .departureDate(booking.getDepartureDateSnapshot() != null ? booking.getDepartureDateSnapshot().toString() : null)
                .departureLocation(booking.getDepartureLocationSnapshot()) // Map Điểm khởi hành

                .note(booking.getNote()) // Map Ghi chú

                .totalOriginalPrice(booking.getTotalOriginalPrice())
                .totalDiscount(booking.getTotalDiscount())
                .totalSurcharge(booking.getTotalSurcharge())
                .totalFinalPrice(booking.getTotalFinalPrice())

                .status(booking.getStatus() != null ? booking.getStatus().name() : null)
                .voucherCode(booking.getVoucher() != null ? booking.getVoucher().getCode() : null)
                .createdAt(booking.getCreatedAt())

                .createdByEmail(booking.getCreatedBy() != null ? booking.getCreatedBy().getEmail() : null)

                .passengers(passengers != null ? passengers.stream()
                        .map(PassengerResponse::fromPassenger)
                        .collect(Collectors.toList()) : List.of())
                .build();
    }
}