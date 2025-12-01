package SpringClass.shop.dto;

import SpringClass.shop.entity.Notifications;
import SpringClass.shop.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ResponseNotification {

    private Long id;
    private NotificationType type;
    private String content;
    private Long targetId;
    private boolean isRead;
    private LocalDateTime createdAt;


    public static ResponseNotification from(Notifications notification) {
        return ResponseNotification.builder()
                .id(notification.getId())
                .type(notification.getNotificationType())
                .content(notification.getContent())
                .targetId(notification.getTargetId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
