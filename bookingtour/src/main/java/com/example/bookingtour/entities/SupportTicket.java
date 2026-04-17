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
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private CustomerProfile customer;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "description", columnDefinition = "text", nullable = false)
    private String description;

    @Column(name = "admin_response", columnDefinition = "text")
    private String adminResponse;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_staff_id")
    private StaffProfile assignedStaff;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 20)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private TicketStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "closed_at")
    private Instant closedAt;
}