package com.example.bookingtour.dtos.request.booking;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class PassengerRequest {

    @NotBlank(message = "Tên hành khách không được để trống")
    private String fullName;

    // "ADULT" hoặc "CHILD"
    @NotBlank(message = "Loại hành khách không được để trống")
    private String passengerType;

    private String gender;

    @NotBlank
    private LocalDate birthDate;
}
