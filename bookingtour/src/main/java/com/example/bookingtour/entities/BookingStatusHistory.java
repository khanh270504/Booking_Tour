package com.example.bookingtour.entities;

import com.example.bookingtour.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "booking_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class BookingStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Liên kết với Booking nào
    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private BookingStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 30)
    private BookingStatus toStatus;

    @Column(name = "reason", columnDefinition = "text")
    private String reason;     // Lý do thay đổi (VD: "Khách chưa thanh toán sau 10p")

    @Column(name = "changed_by", length = 50)
    private String changedBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}