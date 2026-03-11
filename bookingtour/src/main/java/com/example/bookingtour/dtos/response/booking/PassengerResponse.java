package com.example.bookingtour.dtos.response.booking;

import com.example.bookingtour.entities.BookingPassenger;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.parameters.P;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PassengerResponse {
    private Integer id;
    private String fullName;
    private String passengerType;
    private String gender;
    private LocalDate birthDate;
    private BigDecimal unitPrice;

    public static PassengerResponse fromPassenger(BookingPassenger passenger) {
        if (passenger == null) {
            return null;
        }

        return PassengerResponse.builder()
                .id(passenger.getId())
                .fullName(passenger.getFullName())
                .birthDate(passenger.getBirthDate())
                .gender(passenger.getGender())
                .passengerType(passenger.getPassengerType() != null ? passenger.getPassengerType().name() : null)
                .unitPrice(passenger.getUnitPrice())
                .build();

    }
}