package com.example.bookingtour.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "booking_passengers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BookingPassenger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "passenger_type", length = 20)
    private String passengerType; // ADULT, CHILD

    @Column(name = "unit_price")
    private BigDecimal unitPrice; // Giá vé áp dụng cho người này lúc đặt
}