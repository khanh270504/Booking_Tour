package com.example.bookingtour.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "crm_interactions")
@Data
@NoArgsConstructor
public class CrmInteraction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Tương tác với Lead (Khách tiềm năng)
    @ManyToOne
    @JoinColumn(name = "lead_id")
    private CrmLead lead;

    // HOẶC Tương tác với User cũ (Khách đã có tài khoản)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Ai là người thực hiện tương tác này? (Nhân viên Sale)
    @ManyToOne
    @JoinColumn(name = "staff_id")
    private User staff;

    @Column(name = "interaction_type", length = 20)
    private String interactionType; // CALL, MEETING, EMAIL, ZALO

    @Column(name = "note", columnDefinition = "text")
    private String note; // Nội dung cuộc gọi/chat

    @Column(name = "next_action_date")
    private Instant nextActionDate; // Lịch hẹn gọi lại

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}