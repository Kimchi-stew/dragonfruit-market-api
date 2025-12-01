package SpringClass.shop.controller;

import SpringClass.shop.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@AllArgsConstructor
@RequestMapping("/api/alarm")
public class NotificationController {
    private final NotificationService notificationService;

    //Last-Event-ID는 SSE 연결이 끊어졌을 경우,
    // 클라이언트가 수신한 마지막 이벤트의 id 값을 의미 항상 존재하는 것이 아니기 때문에 false
    // 마지막 이벤트 id를 보내면 서버는 그 id 이후의 이벤트를 재전송
    @GetMapping(value = "/connect",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "알림 연결", description = "알림 연결 시 사용하는 API 입니다.")
    public ResponseEntity<SseEmitter> subscribe(
            @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        return ResponseEntity.ok(notificationService.subscribe(lastEventId));
    }

}
