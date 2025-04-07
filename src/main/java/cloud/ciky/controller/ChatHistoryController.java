package cloud.ciky.controller;

import cloud.ciky.entity.vo.MessageVO;
import cloud.ciky.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: ciky
 * @Description: 会话历史接口
 * @DateTime: 2025/4/7 20:28
 **/
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai/history")
public class ChatHistoryController {

    private final ChatHistoryRepository chatHistoryRepository;

    private final ChatMemory chatMemory;

    /**
     * 查询会话历史列表
     * @param type  会话类型(chat,service,pdf)
     * @return      会话id列表
     */
    @GetMapping("/{type}")
    public List<String> getChatIds(@PathVariable("type") String type){
        return chatHistoryRepository.getChatIds(type);
    }

    /**
     * 查询会话历史详情
     * @param type  会话类型(chat,service,pdf)
     * @param chatId    会话ID
     * @return      会话历史详情列表
     */
    @GetMapping("/{type}/{chatId}")
    public List<MessageVO> getChatHistory(@PathVariable("type") String type,@PathVariable("chatId") String chatId){
        List<Message> messages = chatMemory.get(chatId, Integer.MAX_VALUE);
        if(messages == null){
            return List.of();
        }

        return messages.stream().map(m -> new MessageVO(m)).toList();
    }

}
