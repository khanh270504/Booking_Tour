package com.example.bookingtour.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống chưa được phân loại", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),

    USER_EXISTED(1002, "Email hoặc tên đăng nhập đã tồn tại", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Mật khẩu phải có ít nhất {min} ký tự", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Vui lòng đăng nhập để tiếp tục", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "Bạn không có quyền thực hiện hành động này", HttpStatus.FORBIDDEN),
    USER_BLOCKED(1008, "Tài khoản của bạn đã bị khóa", HttpStatus.FORBIDDEN),

    ROLE_NOT_EXISTED(2001, "Vai trò (Role) không tồn tại", HttpStatus.NOT_FOUND),
    ROLE_EXISTED(2002, "Vai trò này đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    DEPARTMENT_NOT_EXISTED(2003, "Phòng ban không tồn tại", HttpStatus.NOT_FOUND),

    TOUR_NOT_FOUND(3001, "Không tìm thấy thông tin Tour", HttpStatus.NOT_FOUND),
    DESTINATION_NOT_FOUND(3002, "Điểm đến không tồn tại", HttpStatus.NOT_FOUND),
    SCHEDULE_NOT_FOUND(3003, "Không tìm thấy lịch khởi hành này", HttpStatus.NOT_FOUND),
    TOUR_NOT_ACTIVE(3004, "Tour này hiện tại đang tạm ngưng hoặc chưa mở bán", HttpStatus.BAD_REQUEST),
    INVALID_DEPARTURE_DATE(3005, "Ngày khởi hành phải lớn hơn ngày hiện tại", HttpStatus.BAD_REQUEST),
    PRICING_NOT_FOUND(3006, "Chưa cấu hình giá bán cho loại hành khách và ngày khởi hành này", HttpStatus.BAD_REQUEST),
    TOUR_HAS_ACTIVE_SCHEDULES(3007, "Không thể thao tác vì Tour vẫn còn lịch trình đang mở bán", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE(3008, "Ngày về không được trước ngày khởi hành", HttpStatus.BAD_REQUEST),
    INVALID_STATUS(3009, "Trạng thái cập nhật không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT(3010, "Số tiền hoặc giá trị không được là số âm", HttpStatus.BAD_REQUEST),
    SURCHARGE_NOT_FOUND(3011, "Không tìm thấy thông tin phụ phí này", HttpStatus.NOT_FOUND),
    TOUR_FULL(3012, "Đã đủ số lượng", HttpStatus.BAD_REQUEST),

    BOOKING_NOT_FOUND(4001, "Không tìm thấy mã đơn đặt Tour", HttpStatus.NOT_FOUND),
    NOT_ENOUGH_SLOTS(4002, "Rất tiếc, lịch trình này không còn đủ số chỗ trống", HttpStatus.BAD_REQUEST),
    BOOKING_STATUS_INVALID(4003, "Trạng thái đơn hàng không hợp lệ để thực hiện thao tác này", HttpStatus.BAD_REQUEST),
    CANNOT_CANCEL_BOOKING(4004, "Không thể hủy đơn hàng (Đã quá hạn hoặc đã thanh toán thành công)", HttpStatus.BAD_REQUEST),

    VOUCHER_NOT_FOUND(5001, "Mã giảm giá không tồn tại", HttpStatus.NOT_FOUND),
    VOUCHER_EXPIRED(5002, "Mã giảm giá đã hết hạn hoặc hết lượt sử dụng", HttpStatus.BAD_REQUEST),
    PAYMENT_FAILED(5003, "Giao dịch thanh toán thất bại", HttpStatus.BAD_REQUEST),
    DUPLICATE_PAYMENT(5004, "Đơn hàng này đã được thanh toán trước đó", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND(5005, "Không tìm thấy thông tin giao dịch thanh toán", HttpStatus.NOT_FOUND),
    PAYMENT_METHOD_INVALID(5006, "Phương thức thanh toán không hợp lệ", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_CANCELLED(5007, "Giao dịch thanh toán này đã bị hủy từ trước", HttpStatus.BAD_REQUEST),
    PAYMENT_AMOUNT_MISMATCH(5008, "Số tiền thanh toán không khớp với giá trị đơn hàng", HttpStatus.BAD_REQUEST),

    LEAD_NOT_FOUND(6001, "Không tìm thấy thông tin khách hàng tiềm năng", HttpStatus.NOT_FOUND),
    TICKET_NOT_FOUND(6002, "Không tìm thấy phiếu hỗ trợ/khiếu nại này", HttpStatus.NOT_FOUND)
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}