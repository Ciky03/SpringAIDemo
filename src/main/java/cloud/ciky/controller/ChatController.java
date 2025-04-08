package cloud.ciky.controller;

import cloud.ciky.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @Author: ciky
 * @Description: chat接口
 * @DateTime: 2025/4/6 14:37
 **/
//@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ChatController {

    //使用lombok自动生成构造方法注入
    @Autowired
    @Qualifier("chatClient")
    private  ChatClient chatClient;

    @Autowired
    private  ChatHistoryRepository chatHistoryRepository;

    @RequestMapping(value = "/chat",produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam("prompt") String prompt,@RequestParam("chatId") String chatId){
        //1.保存会话ID
        chatHistoryRepository.save("chat",chatId);

        //2.请求模型
        return chatClient.prompt()
                .user(prompt)   //传入user提示词
                .advisors(advisorSpec ->
                        advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY,chatId))   //添加会话id到AdvisorContext
                .stream() //流式调用
                .content();
    }
}
