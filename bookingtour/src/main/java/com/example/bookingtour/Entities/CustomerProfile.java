package com.example.bookingtour.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer_profiles")
@Data
@NoArgsConstructor
public class CustomerProfile {
    @Id
    @Column(name = "user_id")
    private String userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", columnDefinition = "text")
    private String address;

    @Column(name = "nationality", length = 50)
    private String nationality;

    @Column(name = "identity_type", length = 20)
    private String identityType;

    @Column(name = "identity_number", length = 50)
    private String identityNumber;

    @Column(name = "loyalty_points")
    private Integer loyaltyPoints = 0;
}
