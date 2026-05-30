
package com.example.bookingtour.entities;
import com.example.bookingtour.enums.InteractionResult;
import com.example.bookingtour.enums.InteractionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "crm_interactions",
        indexes = {
                @Index(name = "idx_crm_interaction_lead", columnList = "lead_id"),
                @Index(name = "idx_crm_interaction_staff", columnList = "staff_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private CrmLead lead;

    // Nhân viên thực hiện tương tác
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id", nullable = false)
    private StaffProfile staff;

    // CALL / EMAIL / ZALO / MEETING
    @Enumerated(EnumType.STRING)
    @Column(name = "interaction_type", nullable = false, length = 30)
    private InteractionType interactionType;

    // SUCCESS / NO_RESPONSE / INTERESTED ...
    @Enumerated(EnumType.STRING)
    @Column(name = "result", length = 30)
    private InteractionResult result;

    // Nội dung trao đổi
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // Thời lượng cuộc gọi (giây)
    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    // Có liên hệ được không
    @Column(name = "contacted_successfully")
    private Boolean contactedSuccessfully;

    // Lịch chăm sóc tiếp theo
    @Column(name = "next_action_date")
    private Instant nextActionDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}

