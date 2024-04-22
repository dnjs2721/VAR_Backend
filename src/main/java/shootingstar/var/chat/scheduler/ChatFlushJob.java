package shootingstar.var.chat.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import shootingstar.var.chat.dto.ChatMessageDto;
import shootingstar.var.chat.entity.ChatMessage;
import shootingstar.var.chat.repository.ChatMessageRepository;
import shootingstar.var.chat.service.ChatRedisService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ── Write-Back 플러시 Job ──
 *
 * @DisallowConcurrentExecution: 이전 Job이 아직 실행 중이면 다음 트리거를 건너뜀
 * 동시에 같은 메시지가 두 번 INSERT 되는 것을 방지
 *
 * 실행 주기: QuartzConfig에서 5분마다 실행으로 설정
 *
 * 처리 흐름:
 *   1. Redis에서 활성 방 목록 조회 & 초기화
 *   2. 각 방의 메시지 조회 & Redis에서 삭제
 *   3. Entity 변환 후 MySQL Bulk INSERT (saveAll)
 */
@Slf4j
@Component
@RequiredArgsConstructor
@DisallowConcurrentExecution
public class ChatFlushJob implements Job {

    private final ChatRedisService chatRedisService;
    private final ChatMessageRepository chatMessageRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("[ChatFlushJob] Write-Back 플러시 시작");
        int totalFlushed = 0;

        try {
            // 1. 메시지가 있는 활성 채팅방 목록 조회
            List<String> activeRooms = chatRedisService.getAndClearActiveRooms();

            if (activeRooms.isEmpty()) {
                log.info("[ChatFlushJob] 플러시할 메시지 없음. 종료.");
                return;
            }

            for (String roomId : activeRooms) {
                // 2. 방별 메시지 Redis에서 꺼내기
                List<ChatMessageDto> messages = chatRedisService.getAndClearMessages(roomId);

                if (messages.isEmpty()) continue;

                // 3. DTO → Entity 변환
                List<ChatMessage> entities = messages.stream()
                        .map(dto -> ChatMessage.builder()
                                .roomId(dto.getRoomId())
                                .senderId(dto.getSenderId())
                                .senderNickname(dto.getSenderNickname())
                                .content(dto.getContent())
                                .createdAt(dto.getCreatedAt())
                                .build())
                        .collect(Collectors.toList());

                // 4. MySQL 벌크 INSERT (단일 트랜잭션)
                chatMessageRepository.saveAll(entities);
                totalFlushed += entities.size();

                log.info("[ChatFlushJob] roomId={} → {}건 저장", roomId, entities.size());
            }

        } catch (Exception e) {
            // Job 실패 시 예외를 던지면 Quartz가 misfire 처리함
            log.error("[ChatFlushJob] 플러시 실패: {}", e.getMessage(), e);
            throw new JobExecutionException(e, false); // false = 즉시 재시도 안 함
        }

        log.info("[ChatFlushJob] 플러시 완료. 총 {}건 저장", totalFlushed);
    }
}
