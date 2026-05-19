package com.example.bookingtour.entities;

import com.example.bookingtour.enums.InteractionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "crm_interactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // lead được chăm sóc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private CrmLead lead;

    // nhân viên thực hiện tương tác
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private StaffProfile staff;

    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", length = 20, nullable = false)
    private InteractionType interactionType;

    // kết quả cuộc gọi/tư vấn
    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // lịch chăm sóc tiếp theo
    @Column(name = "next_action_date")
    private Instant nextActionDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}