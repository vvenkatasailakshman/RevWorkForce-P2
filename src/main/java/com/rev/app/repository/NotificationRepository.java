package com.rev.app.repository;

import com.rev.app.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // Get all notifications for a user
    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    // Get notifications by read status
    List<Notification> findByUser_UserIdAndIsReadOrderByCreatedAtDesc(Long userId, Integer isRead);

    // Count total notifications
    long countByUser_UserId(Long userId);

    // ⭐ NEW → Count unread notifications
    long countByUser_UserIdAndIsRead(Long userId, Integer isRead);
}