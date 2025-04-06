package cloud.ciky.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: ciky
 * @Description: 配置类
 * @DateTime: 2025/4/6 14:29
 **/
@Configuration
public class CommonConfiguration {


    //注意参数中的model就是使用的模型(这里使用了ollama,也可以使用OpenAIChatModel)
    @Bean
    public ChatClient chatClient(OllamaChatModel model) {
        return ChatClient
                .builder(model) //创建chatClient工厂
                .build();       //构建ChatClient实例
    }

}
