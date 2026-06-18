package com.example.bookingtour.dtos.request.email;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingEmailEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type; // "BOOKING_SUCCESS" hoặc "PAYMENT_SUCCESS"
    private String toEmail;

    // Các trường cho Mail Đặt Tour
    private String bookingCode;
    private String tourName;
    private String customerName;
    private String phone;

    // Các trường cho Mail Thanh Toán
    private String subject;
    private String content;
}
