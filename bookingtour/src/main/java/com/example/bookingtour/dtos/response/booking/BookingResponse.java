package com.example.bookingtour.dtos.response.booking;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
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

    // Lồng danh sách hành khách đi kèm
    private List<PassengerResponse> passengers;
}