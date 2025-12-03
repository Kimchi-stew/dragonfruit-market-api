package SpringClass.shop.service;

import SpringClass.shop.dto.NoticeListResponse;
import SpringClass.shop.dto.ResponseNotification;
import SpringClass.shop.entity.Notifications;
import SpringClass.shop.entity.Reviews.Reviews;
import SpringClass.shop.entity.Sellers.Sellers;
import SpringClass.shop.entity.Users;
import SpringClass.shop.enums.NotificationType;
import SpringClass.shop.exceptions.NotificationSendException;
import SpringClass.shop.global.ApiResponse;
import SpringClass.shop.repository.EmitterRepository;
import SpringClass.shop.repository.NotificationRepository;
import SpringClass.shop.security.AuthenticatedUserUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;
    private final AuthenticatedUserUtils authenticatedUserUtils;


    // 연결 지속시간 1시간
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    public SseEmitter subscribe(String lastEventId) {
        Long userId = authenticatedUserUtils.getCurrentUserId();


        String emitterId = createId(userId);
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

        // 최초 연결시 더미데이터 없으면 오류나기 때문에 더미 데이터 생성
        sendToClient(emitter, emitterId, "EventStream Created. [UserId=" + userId + "]");

        // lastEventIdrk 남아있다면 이벤트 재전송
        if (!lastEventId.isEmpty()) {
            Map<String, Object> events = emitterRepository.findAllEventCacheStartWithByUserId(String.valueOf(userId));

            long lastEventTimestamp = extractTimestamp(lastEventId);

            events.forEach((eventId, data) -> {
                if (extractTimestamp(eventId) > lastEventTimestamp) {
                    sendToClient(emitter, eventId, data);
                }
            });
        }
        return emitter;
    }

    // 알림 생성 + 전송
    public void send(Users receiver, NotificationType notificationType, Long targetId, String content) {
        // Notification 엔티티 생성 및 저장
        Notifications notification = notificationRepository.save(
                Notifications.builder()
                        .users(receiver)
                        .notificationType(notificationType)
                        .targetId(targetId)
                        .content(content)
                        .isRead(false)
                        .build()
        );

        String userId = String.valueOf(receiver.getId());

        // 해당 사용자의 모든 SSE 연결 조회
        Map<String, SseEmitter> sseEmitters = emitterRepository.findAllEmitterStartWithByUserId(userId);
        sseEmitters.forEach((key, emitter) -> {
            String eventId = createId(receiver.getId());

            emitterRepository.saveEventCache(eventId, notification);

            sendToClient(
                    emitter,
                    eventId,
                    ApiResponse.<ResponseNotification>builder()
                            .success(true)
                            .message("새로운 알림")
                            .data(ResponseNotification.from(notification))
                            .build()
            );
        });
    }

    // 알림 전송
    private void sendToClient(SseEmitter emitter, String emitterId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(emitterId)
                    .data(data));
        } catch (IOException exception) {
            emitterRepository.deleteById(emitterId);
            throw new NotificationSendException("전송 실패");
        }
    }

    // 내 리뷰에 좋아요 받았을 때 알림
    public void sendReviewLikeNotification(Reviews reviews, String nickname) {
        Users receiver = reviews.getUser(); // 리뷰 작성자
        String content = nickname + "님이 회원님의 리뷰를 좋아합니다.";
        send(
                receiver,
                NotificationType.REVIEW_LIKE,
                reviews.getId(),
                content
        );
    }

    // 내 상점에 팔로우 시 알림
    public void sendSellerFollowNotification(Sellers sellers, String nickname) {
        Users receiver = sellers.getUser(); // 판매자
        String content = nickname + "님이 회원님의 상점을 팔로우 했습니다.";
        send(
                receiver,
                NotificationType.STORE_FOLLOW,
                sellers.getId(),
                content
        );
    }



    private String createId(Long userId) {
        return userId + "_" + System.currentTimeMillis();
    }

    private long extractTimestamp(String eventId) {
        try {
            return Long.parseLong(eventId.split("_")[1]);
        } catch (Exception e) {
            return 0;
        }
    }

    public Page<NoticeListResponse> getNoticeList(Pageable pageable) {
        Users user = authenticatedUserUtils.getCurrentUser();

        Page<Notifications> notifications =
                notificationRepository.findAllByUsersOrderByCreatedAtDesc(user, pageable);

        // 알림 읽음 처리
        notifications.forEach(no -> {
            if (!no.isRead()) {
                no.setRead(true);
            }
        });
        return notifications.map(NoticeListResponse::from);
    }

    public Page<NoticeListResponse> getUnreadNoticeList(Pageable pageable) {
        Users user = authenticatedUserUtils.getCurrentUser();

        Page<Notifications> notifications =
                notificationRepository.findAllByUsersAndIsReadFalseOrderByCreatedAtDesc(user, pageable);
        // 알림 읽음 처리
        notifications.forEach(no -> {
            if (!no.isRead()) {
                no.setRead(true);
            }
        });
        return notifications.map(NoticeListResponse::from);
    }


    public int getUnreadCount() {
        Users user = authenticatedUserUtils.getCurrentUser();
        return notificationRepository.countByUsersAndIsReadFalse(user);
    }



}
