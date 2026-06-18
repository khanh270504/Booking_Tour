package com.example.bookingtour.services;

import com.example.bookingtour.IServices.INotificationService;
import com.example.bookingtour.dtos.response.notification.NotificationResponse;
import com.example.bookingtour.entities.Notification;
import com.example.bookingtour.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements INotificationService {

    private final NotificationRepository notificationRepository;

    private Integer getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) auth.getPrincipal();

            Object idClaim = jwt.getClaim("id");
            if (idClaim == null) {
                idClaim = jwt.getClaim("userId");
            }

            if (idClaim != null) {
                return Integer.parseInt(idClaim.toString());
            }
        }
        throw new RuntimeException("Lỗi xác thực: Vui lòng đăng nhập hệ thống!");
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications() {
        Integer currentUserId = getCurrentUserId();

        // 🌟 LOG 1: Kiểm tra xem Backend bốc được ID của người đang đăng nhập là bao nhiêu
        log.info("[BACKEND CHECK] ID User bốc từ Token ra là: {}", currentUserId);

        List<Notification> entities = notificationRepository.getMyNotifications(currentUserId);

        // 🌟 LOG 2: Kiểm tra xem câu Query trong Repo bốc lên được bao nhiêu bản ghi dưới DB
        log.info("[BACKEND CHECK] Số lượng thông báo tìm thấy trong DB cho User {} là: {}", currentUserId, entities.size());

        return entities.stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        Integer currentUserId = getCurrentUserId();

        List<Notification> allNotis = notificationRepository.getMyNotifications(currentUserId);
        List<Notification> unreadNotis = allNotis.stream().filter(noti -> !noti.isRead()).toList();

        for (Notification noti : unreadNotis) {
            noti.setRead(true); // Cập nhật trạng thái đã đọc
        }

        notificationRepository.saveAll(unreadNotis);
        log.info("✅ Đã đánh dấu đọc tất cả thông báo cho User ID: {}", currentUserId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notiId) {
        Integer currentUserId = getCurrentUserId();
        Notification noti = notificationRepository.findById(notiId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

        if (!noti.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Lỗi bảo mật: Bạn không có quyền xem thông báo này!");
        }

        noti.setRead(true);
        notificationRepository.save(noti);
    }
}