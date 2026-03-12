package com.rev.app.service;

import com.rev.app.dto.NotificationDto;
import java.util.List;

public interface NotificationService {


// Send notification to user
void sendNotification(Long userId, String message, String type);

// Get notifications for specific user
List<NotificationDto> getUserNotifications(Long userId);

// Get all notifications (for admin dashboard)
List<NotificationDto> getAllNotifications();

// Mark notification as read
void markAsRead(Long notificationId);

// ⭐ NEW → Get unread notifications for user
List<NotificationDto> getUnreadNotifications(Long userId);

// ⭐ NEW → Count unread notifications (for bell icon badge)
long getUnreadCount(Long userId);


}
