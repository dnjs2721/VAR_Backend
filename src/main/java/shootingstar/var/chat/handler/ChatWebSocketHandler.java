package shootingstar.var.chat.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import shootingstar.var.chat.dto.ChatMessageDto;
import shootingstar.var.chat.service.ChatRedisService;

import java.time.LocalDateTime;

/**
 *
 * WebSocket STOMP 메시지 수신 → Redis 저장 → 같은 방 구독자에게 브로드캐스트
 * 클라이언트 발행 경로: /pub/chat/message
 * 클라이언트 구독 경로: /sub/chat/room/{roomId}
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketHandler {

    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatRedisService chatRedisService;

    /**
     * 클라이언트가 /pub/chat/message 로 메시지를 보내면 이 메서드가 실행
     * 처리 순서:
     *  1. createdAt 타임스탬프 세팅
     *  2. Redis에 Write-Back 저장 (O(1), 빠름)
     *  3. STOMP를 통해 같은 방 구독자 전체에게 브로드캐스트
     *
     * @param messageDto 클라이언트가 보낸 메시지 (roomId, content 필수)
     */
    @MessageMapping("/chat/message")
    public void handleMessage(@Payload ChatMessageDto messageDto) {
        // createdAt이 없으면 서버 시간으로 설정
        ChatMessageDto stamped = ChatMessageDto.builder()
                .roomId(messageDto.getRoomId())
                .senderId(messageDto.getSenderId())
                .senderNickname(messageDto.getSenderNickname())
                .content(messageDto.getContent())
                .createdAt(LocalDateTime.now())
                .build();

        // ① Redis Write-Back 저장
        chatRedisService.saveMessage(stamped);

        // ② 같은 방 구독자에게 브로드캐스트
        messagingTemplate.convertAndSend(
                "/sub/chat/room/" + stamped.getRoomId(),
                stamped
        );

        log.debug("[WS] 메시지 처리 완료 - roomId={}, sender={}",
                stamped.getRoomId(), stamped.getSenderId());
    }
}
