package com.example.bookingtour.entities;

import com.example.bookingtour.enums.LeadStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "crm_leads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "lead_code", unique = true, length = 20)
    private String leadCode;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "source", length = 50)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private LeadStatus status;

    @Column(name = "interested_tour_id")
    private Integer interestedTourId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private User assignedStaff;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}