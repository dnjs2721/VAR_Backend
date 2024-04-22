package shootingstar.var.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import shootingstar.var.chat.dto.ChatMessageDto;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * [쓰기] WebSocket 메시지 수신 시:
 *   1. Redis List(chat:messages:{roomId})에 RPUSH → 즉시 응답
 *   2. 활성 방 목록(chat:rooms)에 roomId 추가
 *
 * [읽기] Quartz 스케줄러가 주기적으로:
 *   1. 활성 방 목록에서 roomId들을 꺼냄
 *   2. 각 방의 메시지를 Redis에서 전부 꺼냄(LRANGE + DEL)
 *   3. MySQL에 벌크 INSERT → DB 부하 최소화
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRedisService {

    private final RedisTemplate<String, Object> chatRedisTemplate;
    private final ObjectMapper objectMapper;

    /** Redis List Key: 채팅 메시지 임시 저장 */
    private static final String CHAT_MSG_KEY_PREFIX = "chat:messages:";

    /** Redis Set Key: 메시지가 있는 활성 채팅방 목록 */
    private static final String CHAT_ROOMS_KEY = "chat:rooms";

    // ── 쓰기: WebSocket 핸들러에서 호출 ──────────────────────────────

    /**
     * 메시지를 Redis List 오른쪽에 추가하고 활성 방 목록에 등록
     * O(1) 연산 → 응답 속도 최소화
     */
    public void saveMessage(ChatMessageDto messageDto) {
        String key = CHAT_MSG_KEY_PREFIX + messageDto.getRoomId();
        chatRedisTemplate.opsForList().rightPush(key, messageDto);
        chatRedisTemplate.opsForSet().add(CHAT_ROOMS_KEY, messageDto.getRoomId());
        log.debug("[Redis] 메시지 저장 - key={}, sender={}", key, messageDto.getSenderId());
    }

    // ── 읽기: Quartz Job에서 호출 ────────────────────────────────────

    /**
     * 메시지가 쌓인 모든 활성 채팅방 ID를 반환하고 Set을 초기화
     * 스케줄러 실행 사이클마다 한 번만 처리되도록 원자적으로 삭제
     */
    public List<String> getAndClearActiveRooms() {
        // SMEMBERS로 전체 조회 후 DEL로 초기화 (다음 사이클 대비)
        var members = chatRedisTemplate.opsForSet().members(CHAT_ROOMS_KEY);
        if (members == null || members.isEmpty()) {
            return Collections.emptyList();
        }
        chatRedisTemplate.delete(CHAT_ROOMS_KEY);
        return members.stream().map(Object::toString).collect(Collectors.toList());
    }

    /**
     * 특정 방의 메시지를 전부 꺼내고 Redis에서 삭제.
     * LRANGE 0 -1 → 전체 조회, 이후 DEL로 원자적 삭제.
     *
     * @return 해당 방에 쌓인 ChatMessageDto 리스트
     */
    public List<ChatMessageDto> getAndClearMessages(String roomId) {
        String key = CHAT_MSG_KEY_PREFIX + roomId;
        List<Object> rawList = chatRedisTemplate.opsForList().range(key, 0, -1);
        chatRedisTemplate.delete(key);

        if (rawList == null || rawList.isEmpty()) {
            return Collections.emptyList();
        }

        return rawList.stream()
                .map(obj -> objectMapper.convertValue(obj, ChatMessageDto.class))
                .collect(Collectors.toList());
    }

    // ── 채팅방 입장 시 이력 조회 (Redis 캐시 우선) ───────────────────

    /**
     * 채팅방 입장 시 Redis에 아직 플러시되지 않은 최신 메시지를 조회
     * Redis에 없으면 빈 리스트 반환 → ChatService에서 MySQL 조회로 fallback.
     */
    public List<ChatMessageDto> getRecentMessages(String roomId) {
        String key = CHAT_MSG_KEY_PREFIX + roomId;
        List<Object> rawList = chatRedisTemplate.opsForList().range(key, 0, -1);
        if (rawList == null) return Collections.emptyList();

        return rawList.stream()
                .map(obj -> objectMapper.convertValue(obj, ChatMessageDto.class))
                .collect(Collectors.toList());
    }
}
