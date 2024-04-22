package shootingstar.var.chat.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Table(name = "chat_message",
        indexes = {
                @Index(name = "idx_room_created", columnList = "room_id, created_at")
        })
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 채팅방 식별자.
     *
     */
    @Column(name = "room_id", nullable = false)
    private String roomId;

    /** 발신자 ID (JWT에서 추출한 memberId) */
    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    /** 발신자 닉네임 (조회 시 JOIN 없이 바로 표시하기 위해 비정규화) */
    @Column(name = "sender_nickname", nullable = false, length = 50)
    private String senderNickname;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
