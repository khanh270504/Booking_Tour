package com.example.bookingtour.entities;

import com.example.bookingtour.enums.LeadStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
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
    private Integer id;

    @Column(name = "lead_code", unique = true, length = 20)
    private String leadCode;

    @Column(name = "full_name", length = 100, nullable = false)
    private String fullName;

    @Column(name = "phone", length = 20, nullable = false)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "source", length = 50)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private LeadStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interested_tour_id")
    private Tour interestedTour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private StaffProfile assignedStaff;

    // Mức độ tiềm năng
    // HOT, WARM, COLD
    @Column(name = "priority", length = 20)
    private String priority;

    // Khách muốn đi khi nào
    @Column(name = "expected_travel_date")
    private Instant expectedTravelDate;

    // Số người dự kiến
    @Column(name = "estimated_people")
    private Integer estimatedPeople;

    // Ngân sách dự kiến
    @Column(name = "estimated_budget")
    private BigDecimal estimatedBudget;

    // Lần cuối chăm sóc
    @Column(name = "last_contact_at")
    private Instant lastContactAt;

    // Ngày cần follow tiếp
    @Column(name = "next_follow_up_at")
    private Instant nextFollowUpAt;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}