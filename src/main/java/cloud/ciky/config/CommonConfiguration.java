package cloud.ciky.config;

import cloud.ciky.chatModel.AlibabaOpenAiChatModel;
import cloud.ciky.constants.SystemConstants;
import cloud.ciky.tools.CourseTools;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.ai.autoconfigure.openai.OpenAiChatProperties;
import org.springframework.ai.autoconfigure.openai.OpenAiConnectionProperties;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.model.SimpleApiKey;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    @Bean
    public VectorStore vectorStore(OpenAiEmbeddingModel embeddingModel){
        return SimpleVectorStore.builder(embeddingModel).build();
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

    @Bean
    public ChatClient serviceChatClient(AlibabaOpenAiChatModel model, ChatMemory chatMemory, CourseTools courseTools){
        return ChatClient
                .builder(model) //创建chatClient工厂
                .defaultSystem(SystemConstants.SERVICE_SYSTEM_PROMPT)   //系统提示词
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),                      //添加默认的Advisor记录日志
                        new MessageChatMemoryAdvisor(chatMemory)        //添加默认的Advisor会话记忆
                )
                .defaultTools(courseTools)  //添加Tools
                .build();       //构建ChatClient实例
    }

    /**
     * 将SpringAI自动生成OpenAiChatModel的代码
     * 改为自动生成AlibabaOpenAiChatModel
     * (通过调用AlibabaOpenAiChatModel.builder()生成)
     */
    @Bean
    public AlibabaOpenAiChatModel alibabaOpenAiChatModel(OpenAiConnectionProperties commonProperties, OpenAiChatProperties chatProperties, ObjectProvider<RestClient.Builder> restClientBuilderProvider, ObjectProvider<WebClient.Builder> webClientBuilderProvider, ToolCallingManager toolCallingManager, RetryTemplate retryTemplate, ResponseErrorHandler responseErrorHandler, ObjectProvider<ObservationRegistry> observationRegistry, ObjectProvider<ChatModelObservationConvention> observationConvention) {
        String baseUrl = StringUtils.hasText(chatProperties.getBaseUrl()) ? chatProperties.getBaseUrl() : commonProperties.getBaseUrl();
        String apiKey = StringUtils.hasText(chatProperties.getApiKey()) ? chatProperties.getApiKey() : commonProperties.getApiKey();
        String projectId = StringUtils.hasText(chatProperties.getProjectId()) ? chatProperties.getProjectId() : commonProperties.getProjectId();
        String organizationId = StringUtils.hasText(chatProperties.getOrganizationId()) ? chatProperties.getOrganizationId() : commonProperties.getOrganizationId();
        Map<String, List<String>> connectionHeaders = new HashMap<>();
        if (StringUtils.hasText(projectId)) {
            connectionHeaders.put("OpenAI-Project", List.of(projectId));
        }

        if (StringUtils.hasText(organizationId)) {
            connectionHeaders.put("OpenAI-Organization", List.of(organizationId));
        }
        RestClient.Builder restClientBuilder = restClientBuilderProvider.getIfAvailable(RestClient::builder);
        WebClient.Builder webClientBuilder = webClientBuilderProvider.getIfAvailable(WebClient::builder);
        OpenAiApi openAiApi = OpenAiApi.builder().baseUrl(baseUrl).apiKey(new SimpleApiKey(apiKey)).headers(CollectionUtils.toMultiValueMap(connectionHeaders)).completionsPath(chatProperties.getCompletionsPath()).embeddingsPath("/v1/embeddings").restClientBuilder(restClientBuilder).webClientBuilder(webClientBuilder).responseErrorHandler(responseErrorHandler).build();
        //================================================================================================
        //===========================================修改的内容============================================
        AlibabaOpenAiChatModel chatModel = AlibabaOpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(chatProperties.getOptions()).toolCallingManager(toolCallingManager).retryTemplate(retryTemplate).observationRegistry((ObservationRegistry)observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP)).build();
        //================================================================================================

        Objects.requireNonNull(chatModel);
        observationConvention.ifAvailable(chatModel::setObservationConvention);
        return chatModel;
    }
}
