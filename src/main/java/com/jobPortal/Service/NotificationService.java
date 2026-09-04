package com.jobPortal.Service;

import com.jobPortal.DTO.NotificationDTO;
import com.jobPortal.Model.Application;
import com.jobPortal.Model.Notification;
import com.jobPortal.Model.User;
import com.jobPortal.Repository.NotificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public NotificationDTO createNotification(
            User user,
            Application application,
            String type,
            String title,
            String message
    ) {

        Notification notification = Notification.builder()
                .user(user)
                .application(application)
                .type(type)
                .title(title)
                .message(message)
                .read(false)
                .createdAt(new Date())
                .build();

        notification = notificationRepository.save(notification);

        return mapToDTO(notification);
    }

    public List<NotificationDTO> getUserNotifications(Long userId) {

        return notificationRepository
                .findByUserUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotifications(Long userId) {

        return notificationRepository
                .findByUserUserIdAndReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {

        return notificationRepository
                .countByUserUserIdAndReadFalse(userId);
    }

    public void markAsRead(Long notificationId, Long userId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new RuntimeException("Notification not found"));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to access this notification"
            );
        }

        notification.setRead(true);

        notificationRepository.save(notification);
    }

    public void markAllAsRead(Long userId) {

        List<Notification> notifications =
                notificationRepository
                        .findByUserUserIdAndReadFalseOrderByCreatedAtDesc(userId);

        notifications.forEach(notification ->
                notification.setRead(true)
        );

        notificationRepository.saveAll(notifications);
    }

    public void deleteNotification(Long notificationId, Long userId) {

        Notification notification = notificationRepository
                .findById(notificationId)
                .orElseThrow(() ->
                        new RuntimeException("Notification not found"));

        if (!notification.getUser().getUserId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to delete this notification"
            );
        }

        notificationRepository.delete(notification);
    }

    private NotificationDTO mapToDTO(Notification notification) {

        return NotificationDTO.builder()
                .notificationId(notification.getNotificationId())
                .applicationId(
                        notification.getApplication() != null
                                ? notification.getApplication().getApplicationId()
                                : null
                )
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}