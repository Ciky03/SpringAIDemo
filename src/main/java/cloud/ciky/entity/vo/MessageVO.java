package cloud.ciky.entity.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.messages.Message;

/**
 * @Author: ciky
 * @Description: 会话历史详情VO
 * @DateTime: 2025/4/8 1:10
 **/
@Data
@NoArgsConstructor
public class MessageVO {
    private String role;
    private String content;

    /**
     * 从Message中解析出role和context
     */
    public MessageVO(Message message) {
        switch (message.getMessageType()) {
            case USER:
                role = "user";
                break;
            case ASSISTANT:
                role = "assistant";
                break;
            default:
                role = "";
                break;
        }
        this.content = message.getText();
    }
}
