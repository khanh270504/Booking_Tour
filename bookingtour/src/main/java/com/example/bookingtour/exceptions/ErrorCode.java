package com.example.bookingtour.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // ================= 1000: USER & AUTH =================
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống chưa được phân loại", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Dữ liệu đầu vào không hợp lệ", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "Email hoặc tên đăng nhập đã tồn tại", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Mật khẩu phải có ít nhất {min} ký tự", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "Người dùng không tồn tại", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Vui lòng đăng nhập để tiếp tục", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "Bạn không có quyền truy cập chức năng này", HttpStatus.FORBIDDEN),
    USER_BLOCKED(1008, "Tài khoản của bạn đã bị khóa", HttpStatus.FORBIDDEN),
    UNAUTHORIZED_ACTION(1009, "Bạn không có quyền thao tác trên dữ liệu của người khác", HttpStatus.FORBIDDEN), // Lỗi dành cho việc khách sửa đơn của người khác
    USER_NOT_FOUND(1010, "Không tìm thấy thông tin người dùng", HttpStatus.NOT_FOUND),

    // ================= 2000: ADMIN (ROLE & DEPT) =================
    ROLE_NOT_EXISTED(2001, "Vai trò (Role) không tồn tại", HttpStatus.NOT_FOUND),
    ROLE_EXISTED(2002, "Vai trò này đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    DEPARTMENT_NOT_EXISTED(2003, "Phòng ban không tồn tại", HttpStatus.NOT_FOUND),
    DEPARTMENT_EXISTED(2004, "Phòng ban đã tồn tại", HttpStatus.BAD_REQUEST),
    ROLE_IN_USE(2005, "Vai trò này đang được sử dụng, không thể xóa", HttpStatus.BAD_REQUEST), // Dành cho Admin nếu sau này muốn xóa mềm

    // ================= 3000: TOUR & SCHEDULE =================
    TOUR_NOT_FOUND(3001, "Không tìm thấy thông tin Tour", HttpStatus.NOT_FOUND),
    DESTINATION_NOT_FOUND(3002, "Điểm đến không tồn tại", HttpStatus.NOT_FOUND),
    SCHEDULE_NOT_FOUND(3003, "Không tìm thấy lịch khởi hành này", HttpStatus.NOT_FOUND),
    TOUR_NOT_ACTIVE(3004, "Tour này hiện tại đang tạm ngưng hoặc chưa mở bán", HttpStatus.BAD_REQUEST),
    INVALID_DEPARTURE_DATE(3005, "Ngày khởi hành phải lớn hơn ngày hiện tại", HttpStatus.BAD_REQUEST), // Có thể dùng thay cho DEPARTURE_DATE_IN_PAST
    PRICING_NOT_FOUND(3006, "Chưa cấu hình giá bán cho loại hành khách và ngày khởi hành này", HttpStatus.BAD_REQUEST),
    TOUR_HAS_ACTIVE_SCHEDULES(3007, "Không thể thao tác vì Tour vẫn còn lịch trình đang mở bán", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE(3008, "Ngày về không được trước ngày khởi hành", HttpStatus.BAD_REQUEST), // Có thể dùng thay cho RETURN_DATE_BEFORE_DEPARTURE
    INVALID_STATUS(3009, "Trạng thái cập nhật không hợp lệ", HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT(3010, "Số tiền hoặc giá trị không được là số âm", HttpStatus.BAD_REQUEST),
    SURCHARGE_NOT_FOUND(3011, "Không tìm thấy thông tin phụ phí này", HttpStatus.NOT_FOUND),
    TOUR_FULL(3012, "Lịch trình này đã đủ số lượng hành khách", HttpStatus.BAD_REQUEST),

    // Bổ sung thêm từ logic Service
    INVALID_DATE_FORMAT(3013, "Định dạng ngày tháng không hợp lệ hoặc bị bỏ trống", HttpStatus.BAD_REQUEST),
    PRICING_REQUIRED_FOR_SCHEDULE(3014, "Bắt buộc phải có ít nhất 1 cấu hình giá khi tạo lịch trình", HttpStatus.BAD_REQUEST),
    CANNOT_UPDATE_FINAL_STATUS(3015, "Không thể cập nhật trạng thái của lịch trình đã kết thúc hoặc đã hủy", HttpStatus.BAD_REQUEST),
    CANNOT_CANCEL_TOUR_WITH_BOOKINGS(3016, "Không thể hủy lịch trình vì đã có khách hàng đặt chỗ. Vui lòng xử lý hoàn tiền trước", HttpStatus.BAD_REQUEST),

    // ================= 4000: BOOKING =================
    BOOKING_NOT_FOUND(4001, "Không tìm thấy mã đơn đặt Tour", HttpStatus.NOT_FOUND),
    NOT_ENOUGH_SLOTS(4002, "Rất tiếc, lịch trình này không còn đủ số chỗ trống", HttpStatus.BAD_REQUEST),
    BOOKING_STATUS_INVALID(4003, "Trạng thái đơn hàng không hợp lệ để thực hiện thao tác này", HttpStatus.BAD_REQUEST),
    CANNOT_CANCEL_BOOKING(4004, "Không thể hủy đơn hàng (Đã quá hạn hoặc đã thanh toán thành công)", HttpStatus.BAD_REQUEST),

    // ================= 5000: PAYMENT & VOUCHER =================
    VOUCHER_NOT_FOUND(5001, "Mã giảm giá không tồn tại", HttpStatus.NOT_FOUND),
    VOUCHER_EXPIRED(5002, "Mã giảm giá đã hết hạn sử dụng", HttpStatus.BAD_REQUEST),
    PAYMENT_FAILED(5003, "Giao dịch thanh toán thất bại", HttpStatus.BAD_REQUEST),
    DUPLICATE_PAYMENT(5004, "Đơn hàng này đã được thanh toán trước đó", HttpStatus.BAD_REQUEST),
    PAYMENT_NOT_FOUND(5005, "Không tìm thấy thông tin giao dịch thanh toán", HttpStatus.NOT_FOUND),
    PAYMENT_METHOD_INVALID(5006, "Phương thức thanh toán không hợp lệ", HttpStatus.BAD_REQUEST),
    PAYMENT_ALREADY_CANCELLED(5007, "Giao dịch thanh toán này đã bị hủy từ trước", HttpStatus.BAD_REQUEST),
    PAYMENT_AMOUNT_MISMATCH(5008, "Số tiền thanh toán không khớp với giá trị đơn hàng", HttpStatus.BAD_REQUEST),
    VOUCHER_INACTIVE(5009, "Mã giảm giá này hiện đang tạm khóa", HttpStatus.BAD_REQUEST),
    VOUCHER_OUT_OF_STOCK(5010, "Mã giảm giá này đã hết lượt sử dụng", HttpStatus.BAD_REQUEST),
    VOUCHER_CONDITION_NOT_MET(5011, "Đơn hàng chưa đủ điều kiện để áp dụng mã giảm giá này", HttpStatus.BAD_REQUEST),
    VOUCHER_ALREADY_EXISTS(5012, "Mã giảm giá này đã tồn tại trong hệ thống", HttpStatus.BAD_REQUEST),
    VOUCHER_NOT_YET_STARTED(5013, "Mã giảm giá này chưa đến thời gian áp dụng", HttpStatus.BAD_REQUEST),
    VOUCHER_NOT_FOR_THIS_TOUR(5014, "Mã giảm giá này không áp dụng cho Tour bạn đang chọn", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED_VOUCHER_ACCESS(5015, "Vui lòng đăng nhập để sử dụng mã giảm giá ưu đãi này", HttpStatus.UNAUTHORIZED),
    VOUCHER_NOT_FOR_YOU(5016, "Mã giảm giá này là ưu đãi dành riêng cho tài khoản khác", HttpStatus.FORBIDDEN),
    ORDER_TOTAL_NOT_ENOUGH(5017, "Giá trị đơn hàng chưa đủ mức tối thiểu để áp dụng mã giảm giá này", HttpStatus.BAD_REQUEST),

    // ================= 6000: CRM TICKETS & LEADS =================
    LEAD_NOT_FOUND(6001, "Không tìm thấy thông tin khách hàng tiềm năng", HttpStatus.NOT_FOUND),
    TICKET_NOT_FOUND(6002, "Không tìm thấy phiếu hỗ trợ/khiếu nại này", HttpStatus.NOT_FOUND),

    // ================= 7000: REVIEWS (ĐÁNH GIÁ) =================
    REVIEW_NOT_FOUND(7001, "Không tìm thấy bài đánh giá này", HttpStatus.NOT_FOUND),
    REVIEW_NOT_ALLOWED(7002, "Chỉ được đánh giá sau khi đã hoàn thành chuyến đi", HttpStatus.BAD_REQUEST),
    REVIEW_ALREADY_EXISTS(7003, "Bạn đã đánh giá chuyến đi này rồi", HttpStatus.BAD_REQUEST),
    REVIEW_ALREADY_REPLIED(7004, "Đánh giá này đã được Ban quản trị phản hồi", HttpStatus.BAD_REQUEST),

    // ================= 8000: PROVIDERS & COSTS =================
    PROVIDER_NOT_FOUND(8001, "Không tìm thấy nhà cung cấp này", HttpStatus.NOT_FOUND),
    TOUR_COST_NOT_FOUND(8002, "Không tìm thấy chi phí tour này", HttpStatus.NOT_FOUND),
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