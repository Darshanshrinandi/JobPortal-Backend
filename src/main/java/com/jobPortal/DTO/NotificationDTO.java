package com.jobPortal.DTO;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {

    private Long notificationId;

    private Long applicationId;

    private String type;

    private String title;

    private String message;

    private boolean read;

    private Date createdAt;
}