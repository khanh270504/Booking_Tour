package com.example.bookingtour.dtos.request.booking;

import com.example.bookingtour.enums.PassengerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "Loại hành khách không được để trống")
    private PassengerType passengerType;

    private String gender;

    @NotNull(message = "Ngày sinh không được để trống")
    private LocalDate birthDate;
}
