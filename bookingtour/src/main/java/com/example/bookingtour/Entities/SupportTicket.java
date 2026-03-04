package com.example.bookingtour.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Entity
@Table(name = "support_tickets")
@Data
@NoArgsConstructor
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

    @Column(name = "priority", length = 20)
    private String priority; // HIGH, MEDIUM, LOW

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private String status; // OPEN, CLOSED

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}