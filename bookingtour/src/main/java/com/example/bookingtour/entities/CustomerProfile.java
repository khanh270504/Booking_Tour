package com.example.bookingtour.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CustomerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "user_internal_id")
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
