package com.jobPortal.Controller;

import com.jobPortal.DTO.ApiResponse;
import com.jobPortal.DTO.NotificationDTO;
import com.jobPortal.Service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobPortal/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>>
    getUserNotifications(@PathVariable Long userId) {

        List<NotificationDTO> notifications =
                notificationService.getUserNotifications(userId);

        ApiResponse<List<NotificationDTO>> response =
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Notifications fetched successfully",
                        notifications
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<ApiResponse<List<NotificationDTO>>>
    getUnreadNotifications(@PathVariable Long userId) {

        List<NotificationDTO> notifications =
                notificationService.getUnreadNotifications(userId);

        ApiResponse<List<NotificationDTO>> response =
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Unread notifications fetched successfully",
                        notifications
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<ApiResponse<Long>>
    getUnreadCount(@PathVariable Long userId) {

        long count =
                notificationService.getUnreadCount(userId);

        ApiResponse<Long> response =
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Unread notification count fetched successfully",
                        count
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>>
    markAsRead(
            @PathVariable Long notificationId,
            @RequestParam Long userId
    ) {

        notificationService.markAsRead(
                notificationId,
                userId
        );

        ApiResponse<Void> response =
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Notification marked as read",
                        null
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<ApiResponse<Void>>
    markAllAsRead(@PathVariable Long userId) {

        notificationService.markAllAsRead(userId);

        ApiResponse<Void> response =
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "All notifications marked as read",
                        null
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse<Void>>
    deleteNotification(
            @PathVariable Long notificationId,
            @RequestParam Long userId
    ) {

        notificationService.deleteNotification(
                notificationId,
                userId
        );

        ApiResponse<Void> response =
                new ApiResponse<>(
                        HttpStatus.OK.value(),
                        "Notification deleted",
                        null
                );

        return ResponseEntity.ok(response);
    }
}