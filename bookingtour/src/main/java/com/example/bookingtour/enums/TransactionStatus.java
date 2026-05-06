package com.example.bookingtour.enums;

public enum TransactionStatus {
    PENDING,    // Đang chờ xử lý (Thường dùng khi gọi qua VNPay/Momo chờ callback)
    SUCCESS,    // Giao dịch thành công
    FAILED,     // Giao dịch thất bại (Lỗi thẻ, hủy bỏ, v.v.)
    CANCELLED   // Giao dịch bị admin chủ động hủy (hủy bill thủ công)
}
