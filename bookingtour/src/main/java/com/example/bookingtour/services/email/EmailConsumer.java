package com.example.bookingtour.services.email;
import com.example.bookingtour.configurations.RabbitMQConfig;
import com.example.bookingtour.dtos.request.email.BookingEmailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    // Tiêm trực tiếp  EmailService
    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void listenEmailQueue(BookingEmailEvent event) {
        log.info(" [RabbitMQ Worker] Nhận được yêu cầu gửi mail: {} cho {}", event.getType(), event.getToEmail());

        try {
            if ("BOOKING_SUCCESS".equals(event.getType())) {
                emailService.sendBookingEmail(
                        event.getToEmail(),
                        event.getBookingCode(),
                        event.getTourName(),
                        event.getCustomerName(),
                        event.getPhone()
                );
            } else if ("PAYMENT_SUCCESS".equals(event.getType())) {
                emailService.sendSimpleEmail(
                        event.getToEmail(),
                        event.getSubject(),
                        event.getContent()
                );
            }
            log.info("[RabbitMQ Worker] Đã xử lý xong xuôi luồng gửi Mail!");
        } catch (Exception e) {
            log.error(" Lỗi ngắt quãng khi Worker gọi EmailService: {}", e.getMessage());
        }
    }
}