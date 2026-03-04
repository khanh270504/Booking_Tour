package com.example.bookingtour.dtos.request.booking;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PassengerRequest {

    @NotBlank(message = "Tên hành khách không được để trống")
    private String fullName;

    // "ADULT" hoặc "CHILD"
    @NotBlank(message = "Loại hành khách không được để trống")
    private String passengerType;

    private String gender;

    @NotBlank
    private String birthDate;
}
