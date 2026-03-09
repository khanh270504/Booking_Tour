package com.example.bookingtour.entities;

import com.example.bookingtour.enums.ServiceType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "providers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", length = 50)
    private ServiceType serviceType; // HOTEL, TRANSPORT

    @Column(name = "contact_info", columnDefinition = "text")
    private String contactInfo;
}