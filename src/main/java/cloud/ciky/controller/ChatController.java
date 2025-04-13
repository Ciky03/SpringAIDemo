package cloud.ciky.controller;

import cloud.ciky.repository.ChatHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor;
import org.springframework.ai.model.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

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
    private ChatClient chatClient;

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @RequestMapping(value = "/chat", produces = "text/html;charset=utf-8")
    public Flux<String> chat(
            @RequestParam("prompt") String prompt,
            @RequestParam("chatId") String chatId,
            @RequestParam(value = "files",required = false) List<MultipartFile> files) {
        //1.保存会话ID
        chatHistoryRepository.save("chat", chatId);

        //2.请求模型
        if (files == null || files.isEmpty()) {
            //没有附件,纯文本
            return textChat(prompt, chatId);
        } else {
            //有附件,多模态聊天
            return multiModalChat(prompt, chatId, files);
        }

    }

    /**
     * 多模态聊天
     */
    private Flux<String> multiModalChat(String prompt, String chatId, List<MultipartFile> files) {
        //1.解析多媒体
        List<Media> medias = files.stream()
                .map(file -> new Media(
                                MimeType.valueOf(file.getContentType()),
                                file.getResource()
                        )
                )
                .toList();

        //2.请求模型
        return chatClient.prompt()
                .user(p->p.text(prompt).media(medias.toArray(Media[]::new)))   //传入user提示词
                .advisors(advisorSpec ->
                        advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)    //添加会话id到AdvisorContext
                )
                .stream() //流式调用
                .content();
    }

    /**
     * 纯文本聊天
     */
    private Flux<String> textChat(String prompt, String chatId) {
        //请求模型
        return chatClient.prompt()
                .user(prompt)   //传入user提示词
                .advisors(advisorSpec ->
                        advisorSpec.param(AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))   //添加会话id到AdvisorContext
                .stream() //流式调用
                .content();
    }
}
