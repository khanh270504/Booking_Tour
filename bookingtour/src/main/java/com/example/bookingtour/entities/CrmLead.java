package com.example.bookingtour.entities;

import com.example.bookingtour.enums.LeadPriority;
import com.example.bookingtour.enums.LeadSource;
import com.example.bookingtour.enums.LeadStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "crm_leads",
        indexes = {
                @Index(name = "idx_crm_lead_phone", columnList = "phone"),
                @Index(name = "idx_crm_lead_status", columnList = "status"),
                @Index(name = "idx_crm_lead_staff", columnList = "assigned_staff_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmLead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lead_code", unique = true, nullable = false, length = 20)
    private String leadCode;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    // Nguồn khách hàng
    @Enumerated(EnumType.STRING)
    @Column(name = "source", length = 30)
    private LeadSource source;

    // Trạng thái pipeline CRM
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LeadStatus status;

    // Tour khách đang quan tâm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interested_tour_id")
    private Tour interestedTour;

    // Sale phụ trách
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private StaffProfile assignedStaff;

    // HOT / WARM / COLD
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private LeadPriority priority;

    // Khách dự kiến đi ngày nào
    @Column(name = "expected_travel_date")
    private LocalDate expectedTravelDate;

    // Số người dự kiến
    @Column(name = "estimated_people")
    private Integer estimatedPeople;

    // Ngân sách dự kiến
    @Column(name = "estimated_budget", precision = 15, scale = 2)
    private BigDecimal estimatedBudget;

    // Lần cuối chăm sóc
    @Column(name = "last_contact_at")
    private Instant lastContactAt;

    // Lý do mất khách
    @Column(name = "lost_reason", columnDefinition = "TEXT")
    private String lostReason;

    // Ghi chú nội bộ
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
