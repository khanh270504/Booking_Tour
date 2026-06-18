package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.response.notification.NotificationResponse;
import java.util.List;

public interface INotificationService {
    List<NotificationResponse> getMyNotifications();

    void markAllAsRead();

    void markAsRead(Long notiId);
}