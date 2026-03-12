package com.rev.app.service;

import com.rev.app.dto.NotificationDto;
import com.rev.app.entity.Notification;
import com.rev.app.entity.User;
import com.rev.app.mapper.DTOMapper;
import com.rev.app.repository.NotificationRepository;
import com.rev.app.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationServiceImpl implements NotificationService {


@Autowired
private NotificationRepository notificationRepository;

@Autowired
private UserRepository userRepository;

@Autowired
private DTOMapper dtoMapper;

// ================= SEND NOTIFICATION =================
@Override
@Transactional
public void sendNotification(Long userId, String message, String type) {

    User user = userRepository.findById(userId).orElse(null);

    if (user != null) {

        Notification notification = new Notification(
                null,
                user,
                message,
                type,
                0,
                null
        );

        notificationRepository.save(notification);
    }
}

// ================= GET USER NOTIFICATIONS =================
@Override
public List<NotificationDto> getUserNotifications(Long userId) {

    return notificationRepository
            .findByUser_UserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(dtoMapper::toNotificationDto)
            .collect(Collectors.toList());
}

// ================= GET ALL NOTIFICATIONS =================
@Override
public List<NotificationDto> getAllNotifications() {

    return notificationRepository
            .findAll()
            .stream()
            .map(dtoMapper::toNotificationDto)
            .collect(Collectors.toList());
}

// ================= MARK AS READ =================
@Override
@Transactional
public void markAsRead(Long notificationId) {

    notificationRepository.findById(notificationId).ifPresent(n -> {

        n.setIsRead(1);

        notificationRepository.save(n);

    });
}

// ⭐ NEW → GET UNREAD NOTIFICATIONS
@Override
public List<NotificationDto> getUnreadNotifications(Long userId) {

    return notificationRepository
            .findByUser_UserIdAndIsReadOrderByCreatedAtDesc(userId, 0)
            .stream()
            .map(dtoMapper::toNotificationDto)
            .collect(Collectors.toList());
}

// ⭐ NEW → COUNT UNREAD NOTIFICATIONS
@Override
public long getUnreadCount(Long userId) {

    return notificationRepository
            .findByUser_UserIdAndIsReadOrderByCreatedAtDesc(userId, 0)
            .size();
}


}
