
package com.example.bookingtour.entities;

import com.example.bookingtour.enums.TaskPriority;
import com.example.bookingtour.enums.TaskStatus;
import com.example.bookingtour.enums.TaskType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "crm_tasks",
        indexes = {
                @Index(name = "idx_crm_task_staff", columnList = "assigned_staff_id"),
                @Index(name = "idx_crm_task_status", columnList = "status"),
                @Index(name = "idx_crm_task_due_date", columnList = "due_date")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrmTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Tiêu đề công việc
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    // Mô tả chi tiết
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Lead liên quan
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private CrmLead lead;

    // Nhân viên phụ trách
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id", nullable = false)
    private StaffProfile assignedStaff;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 30)
    private TaskType taskType;

    // TODO / IN_PROGRESS / DONE / CANCELLED
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status;

    // LOW / MEDIUM / HIGH / URGENT
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private TaskPriority priority;

    // Hạn xử lý
    @Column(name = "due_date")
    private Instant dueDate;

    // Thời gian bắt đầu xử lý
    @Column(name = "started_at")
    private Instant startedAt;

    // Thời gian hoàn thành
    @Column(name = "completed_at")
    private Instant completedAt;

    // Task quá hạn hay không
    @Column(name = "is_overdue")
    @Builder.Default
    private Boolean overdue = false;

    // Ghi chú nội bộ
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}

