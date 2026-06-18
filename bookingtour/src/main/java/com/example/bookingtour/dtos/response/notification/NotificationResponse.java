package com.example.bookingtour.dtos.response.notification;

import com.example.bookingtour.entities.Notification;
import com.example.bookingtour.enums.NotificationType;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private NotificationType type;
    private Integer userId;
    private boolean read;
    private LocalDateTime createdAt;
    private String createdBy;

    public static NotificationResponse fromEntity(Notification noti) {
        if (noti == null) return null;

        return NotificationResponse.builder()
                .id(noti.getId())
                .title(noti.getTitle())
                .message(noti.getMessage())
                .type(noti.getType())
                .userId(noti.getUser() != null ? noti.getUser().getId() : null) // 🌟 Đye, bọc check null cho chắc chắn
                .read(noti.isRead())
                .createdAt(noti.getCreatedAt())
                .createdBy(noti.getCreatedBy())
                .build();
    }
}