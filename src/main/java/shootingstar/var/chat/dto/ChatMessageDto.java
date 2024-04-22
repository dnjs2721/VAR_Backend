package shootingstar.var.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 *
 * Redis에 직렬화 저장되고, WebSocket으로 클라이언트에게 전송되는 DTO.
 *
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto implements Serializable {

    private String roomId;
    private Long senderId;
    private String senderNickname;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
