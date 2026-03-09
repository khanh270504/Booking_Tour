package com.example.bookingtour.entities;

import com.example.bookingtour.enums.TicketPriority;
import com.example.bookingtour.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(name = "support_tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SupportTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "subject")
    private String subject;

    // Nhân viên nào đang xử lý ticket này
    @ManyToOne
    @JoinColumn(name = "assigned_staff")
    private User assignedStaff;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private TicketPriority priority; // HIGH, MEDIUM, LOW

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TicketStatus status; // OPEN, CLOSED

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}