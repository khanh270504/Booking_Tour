package com.example.bookingtour.services.email;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendBookingEmail(String to, String bookingCode, String tourName, String customerName, String phone) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Xác nhận đặt tour thành công - " + bookingCode);

            String html = buildEmailTemplate(
                    bookingCode,
                    tourName,
                    customerName,
                    to,
                    phone
            );
            helper.setText(html, true);

            mailSender.send(message);

        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException("Lỗi gửi email booking", e);
        }
    }

    // 🌟 HÀM MỚI: GỬI EMAIL THÔNG BÁO TÀI CHÍNH / THANH TOÁN
    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);

            String html = buildPaymentEmailTemplate(subject, content);
            helper.setText(html, true);

            mailSender.send(message);
        } catch (jakarta.mail.MessagingException e) {
            throw new RuntimeException("Lỗi gửi email thanh toán", e);
        }
    }

    private String buildEmailTemplate(String bookingCode, String tourName, String customerName, String email, String phone) {
        return """
    <div style="font-family:Arial;background:#f4f4f4;padding:20px">
        <div style="max-width:600px;margin:auto;background:#fff;padding:20px;border-radius:10px">

            <h2 style="color:#2c3e50">🎉 Đặt tour thành công!</h2>

            <hr/>

            <h3> Thông tin khách hàng</h3>
            <p><b>Họ tên:</b> %s</p>
            <p><b>Email:</b> %s</p>
            <p><b>Số điện thoại:</b> %s</p>

            <hr/>

            <h3> Thông tin booking</h3>
            <p><b>Mã booking:</b> %s</p>
            <p><b>Tên tour:</b> %s</p>

            <div style="margin-top:15px;padding:10px;background:#eaf2ff;border-radius:8px">
                 Vui lòng lưu lại mã booking để tra cứu sau này.
            </div>

            <p style="margin-top:20px;font-size:12px;color:#888">
                Email này được gửi tự động, vui lòng không trả lời.
            </p>

        </div>
    </div>
    """.formatted(customerName, email, phone, bookingCode, tourName);
    }

    private String buildPaymentEmailTemplate(String title, String content) {
        String headerColor = (title.contains("Cảnh báo") || title.contains("hủy")) ? "#e74c3c" : "#2ecc71";

        return """
    <div style="font-family:Arial;background:#f4f4f4;padding:20px">
        <div style="max-width:600px;margin:auto;background:#fff;padding:20px;border-radius:10px">

            <h2 style="color:%s">%s</h2>

            <hr style="border:none;border-top:1px solid #eee;margin:15px 0;"/>

            <div style="font-size:15px;color:#333;line-height:1.6;white-space:pre-line">
                %s
            </div>

            <hr style="border:none;border-top:1px solid #eee;margin:20px 0;"/>

            <div style="padding:10px;background:#f9f9f9;border-radius:8px;font-size:13px;color:#666">
                 Mọi thắc mắc về vấn đề tài chính đối soát, vui lòng liên hệ bộ phận Kế toán qua Hotline 1900 888 888 để được hỗ trợ nhanh nhất.
            </div>

            <p style="margin-top:20px;font-size:12px;color:#888">
                Email này được gửi tự động từ hệ thống tài chính BookingTour, vui lòng không trả lời thư này.
            </p>

        </div>
    </div>
    """.formatted(headerColor, title, content);
    }
}