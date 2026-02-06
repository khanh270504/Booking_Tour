package com.example.bookingtour.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "booking_status_history")
@Data
@NoArgsConstructor
public class BookingStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Liên kết với Booking nào
    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "from_status", length = 30)
    private String fromStatus; // Trạng thái cũ (VD: PENDING)

    @Column(name = "to_status", length = 30)
    private String toStatus;   // Trạng thái mới (VD: CANCELLED)

    @Column(name = "reason", columnDefinition = "text")
    private String reason;     // Lý do thay đổi (VD: "Khách chưa thanh toán sau 10p")

    // Lưu User ID dạng String thay vì quan hệ User
    // Lý do: Để nếu sau này User bị xóa, lịch sử này vẫn còn nguyên tên người đó
    // Hoặc lưu 'SYSTEM' nếu là hệ thống tự hủy
    @Column(name = "changed_by", length = 50)
    private String changedBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}