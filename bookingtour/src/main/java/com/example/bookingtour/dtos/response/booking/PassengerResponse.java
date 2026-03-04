package com.example.bookingtour.dtos.response.booking;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PassengerResponse {
    private Integer id;
    private String fullName;
    private String passengerType;
    private String gender;
    private LocalDate birthDate;
    private BigDecimal unitPrice; // Giá lúc đặt
}