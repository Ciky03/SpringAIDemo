package cloud.ciky.controller;

import cloud.ciky.repository.ChatHistoryRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @Author: ciky
 * @Description: TODO
 * @DateTime: 2025/4/9 1:23
 **/
@RestController
@RequestMapping("/ai")
public class CustomerServiceController {

    private final ChatClient serviceChatClient;

    private final ChatHistoryRepository chatHistoryRepository;

    //构造方法注入
    public CustomerServiceController(@Qualifier("serviceChatClient") ChatClient serviceChatClient, ChatHistoryRepository chatHistoryRepository) {
        this.serviceChatClient = serviceChatClient;
        this.chatHistoryRepository = chatHistoryRepository;
    }

    @RequestMapping(value = "/service",produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam("prompt") String prompt, @RequestParam("chatId") String chatId) {
        //1.保存会话ID
        chatHistoryRepository.save("service",chatId);
        //2.请求模型
        return serviceChatClient.prompt()
                .user(prompt)   //传入user提示词
                .advisors(advisorSpec ->
                        advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY,chatId))   //添加会话id到AdvisorContext
                .stream()
                .content();
    }
}
