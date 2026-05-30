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
    private String buildEmailTemplate(String bookingCode, String tourName, String customerName, String email, String phone) {
        return """
    <div style="font-family:Arial;background:#f4f4f4;padding:20px">
        <div style="max-width:600px;margin:auto;background:#fff;padding:20px;border-radius:10px">

            <h2 style="color:#2c3e50">🎉 Đặt tour thành công!</h2>

            <hr/>

            <h3>👤 Thông tin khách hàng</h3>
            <p><b>Họ tên:</b> %s</p>
            <p><b>Email:</b> %s</p>
            <p><b>Số điện thoại:</b> %s</p>

            <hr/>

            <h3>🧾 Thông tin booking</h3>
            <p><b>Mã booking:</b> %s</p>
            <p><b>Tên tour:</b> %s</p>

            <div style="margin-top:15px;padding:10px;background:#eaf2ff;border-radius:8px">
                💡 Vui lòng lưu lại mã booking để tra cứu sau này.
            </div>

            <p style="margin-top:20px;font-size:12px;color:#888">
                Email này được gửi tự động, vui lòng không trả lời.
            </p>

        </div>
    </div>
    """.formatted(customerName, email, phone, bookingCode, tourName);
    }
}
