package cloud.ciky.repository;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: ciky
 * @Description: 会话历史存储实现类 (内存存储)
 * @DateTime: 2025/4/7 20:21
 **/
@Component
public class InMemoryChatHistoryRepository implements ChatHistoryRepository{

    private final Map<String,List<String>> chatHistory = new HashMap<>();
    @Override
    public void save(String type, String chatId) {
        //type不存在-->放入空集合
        if(!chatHistory.containsKey(type)){
            chatHistory.put(type,new ArrayList<>());
        }
        //获取会话ID列表
        List<String> chatIds = chatHistory.get(type);
        //判断会话ID是否存在
        if(chatIds.contains(chatId)){
            return;
        }
        //将会话ID存入列表
        chatIds.add(chatId);
    }

    @Override
    public List<String> getChatIds(String type) {
        List<String> chatIds = chatHistory.get(type);
        return chatIds == null ? List.of() : chatIds;
    }
}
