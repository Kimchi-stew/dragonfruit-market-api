package SpringClass.shop.dto;

import SpringClass.shop.entity.Notifications;
import SpringClass.shop.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NoticeListResponse {
    private Long id;
    private NotificationType type;
    private String content;
    private Long targetId;
    private LocalDateTime createdAt;
    private boolean isRead; // 읽음 여부

    public static NoticeListResponse from(Notifications notification) {
        return NoticeListResponse.builder()
                .id(notification.getId())
                .type(notification.getNotificationType())
                .content(notification.getContent())
                .targetId(notification.getTargetId())
                .createdAt(notification.getCreatedAt())
                .isRead(notification.isRead())
                .build();
    }
}
