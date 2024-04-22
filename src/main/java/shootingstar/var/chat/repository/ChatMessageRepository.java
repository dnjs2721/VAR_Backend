package shootingstar.var.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import shootingstar.var.chat.entity.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * 채팅방의 메시지 이력 조회 (최신순 50개).
     * 클라이언트가 채팅방 입장 시 기존 이력을 가져올 때 사용
     */
    @Query("SELECT m FROM ChatMessage m WHERE m.roomId = :roomId ORDER BY m.createdAt DESC")
    List<ChatMessage> findTop50ByRoomId(@Param("roomId") String roomId,
                                        org.springframework.data.domain.Pageable pageable);
}
