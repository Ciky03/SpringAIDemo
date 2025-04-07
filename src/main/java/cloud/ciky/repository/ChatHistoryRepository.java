package cloud.ciky.repository;

import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: ciky
 * @Description: 会话历史存储接口
 * @DateTime: 2025/4/7 20:18
 **/
@Component
public interface ChatHistoryRepository {

    /**
     * 保存会话记录
     * @param type    会话类型(chat,service,pdf)
     * @param chatId  会话id
     */
    void save(String type, String chatId);

    /**
     * 获取会话ID列表
     * @param type  会话类型(chat,service,pdf)
     * @return  会话ID列表
     */
    List<String> getChatIds(String type);
}
