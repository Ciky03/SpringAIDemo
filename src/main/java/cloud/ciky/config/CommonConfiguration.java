package cloud.ciky.config;

import cloud.ciky.constants.SystemConstants;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
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
//    @Bean
//    public ChatClient chatClient(OllamaChatModel model) {
//        return ChatClient
//                .builder(model) //创建chatClient工厂
//                .defaultSystem("你是一个智能助手,你的名字叫Ciky")
//                .build();       //构建ChatClient实例
//    }

    @Bean
    public ChatMemory chatMemory(){
        return new InMemoryChatMemory();
    }


    //使用openAI模型
    @Bean
    public ChatClient chatClient(OpenAiChatModel model,ChatMemory chatMemory){
        return ChatClient
                .builder(model) //创建chatClient工厂
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),                  //添加默认的Advisor记录日志
                        new MessageChatMemoryAdvisor(chatMemory)    //添加默认的Advisor会话记忆
                )
                .build();       //构建ChatClient实例
    }


    //使用openAI模型
    @Bean
    public ChatClient gameChatClient(OpenAiChatModel model,ChatMemory chatMemory){
        return ChatClient
                .builder(model) //创建chatClient工厂
                .defaultSystem(SystemConstants.GAME_SYSTEM_PROMPT)	//系统提示词
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),                  //添加默认的Advisor记录日志
                        new MessageChatMemoryAdvisor(chatMemory)    //添加默认的Advisor会话记忆
                )
                .build();       //构建ChatClient实例
    }
}
