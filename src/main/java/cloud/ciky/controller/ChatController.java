package cloud.ciky.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @Author: ciky
 * @Description: chat接口
 * @DateTime: 2025/4/6 14:37
 **/
@RequiredArgsConstructor
@RestController
@RequestMapping("/ai")
public class ChatController {

    //使用lombok自动生成构造方法注入
    private final ChatClient chatClient;

    @RequestMapping(value = "/chat",produces = "text/html;charset=utf-8")
    public Flux<String> chat(@RequestParam("prompt") String prompt){
        return chatClient.prompt()
                .user(prompt)   //传入user提示词
                .stream() //流式调用
                .content();
    }
}
