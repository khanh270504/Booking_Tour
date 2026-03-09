package com.example.bookingtour.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "report_daily_revenue")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ReportDailyRevenue {

    // Khóa chính là Ngày (Mỗi ngày chỉ có 1 dòng báo cáo)
    @Id
    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "total_revenue")
    private BigDecimal totalRevenue; // Tổng tiền thu về

    @Column(name = "total_bookings")
    private Integer totalBookings;   // Tổng số đơn hàng

    @Column(name = "total_profit")
    private BigDecimal totalProfit;  // Lợi nhuận (Doanh thu - Chi phí)

    // Thời điểm cập nhật báo cáo lần cuối
    // Dùng @UpdateTimestamp để mỗi khi chạy lại Job tính toán nó tự update giờ
    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}