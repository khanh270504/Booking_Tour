package com.example.bookingtour.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "crm_leads")
@Data
@NoArgsConstructor
public class CrmLead {
    @Id
    @Column(name = "id", length = 20)
    private String id;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "source", length = 50)
    private String source; // FACEBOOK, ZALO, WEBSITE

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "interested_tour_id")
    private Integer interestedTourId; // Tour khách đang hỏi

    // Sale nào đang chăm sóc lead này?
    @ManyToOne
    @JoinColumn(name = "assigned_staff_id")
    private User assignedStaff;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}