package com.example.bookingtour.Enums;

public enum LeadStatus {
    NEW,            // Khách mới đổ về, chưa ai gọi
    CONTACTED,      // Đã gọi thoại/nhắn tin
    QUALIFIED,      // Khách rất tiềm năng, chốt tỷ lệ cao
    LOST,           // Khách bom, khách từ chối
    CONVERTED       // Đã chốt thành công đơn hàng
}