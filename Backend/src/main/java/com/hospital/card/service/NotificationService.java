package com.hospital.card.service;

import com.hospital.card.entity.Notification;
import com.hospital.card.entity.User;
import com.hospital.card.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public void createNotification(User user, String title, String message, String type, Long entityId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setEntityId(entityId);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }

    public void createNotification(User user, String title, String message, String type) {
        createNotification(user, title, message, type, null);
    }
}
