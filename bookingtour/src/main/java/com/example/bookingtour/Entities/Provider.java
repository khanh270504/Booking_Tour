package com.example.bookingtour.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "providers")
@Data
@NoArgsConstructor
public class Provider {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "service_type", length = 50)
    private String serviceType; // HOTEL, TRANSPORT

    @Column(name = "contact_info", columnDefinition = "text")
    private String contactInfo;
}