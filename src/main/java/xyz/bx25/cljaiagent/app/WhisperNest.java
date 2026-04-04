package xyz.bx25.cljaiagent.app;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import xyz.bx25.cljaiagent.advisor.MyLoggerAdvisor;
import xyz.bx25.cljaiagent.memory.FileBasedChatMemoryRepository;
import xyz.bx25.cljaiagent.prompt.WhisperNestPrompt;
import xyz.bx25.cljaiagent.rag.WhisperNestCloudAdvisorConfig;

/**
 * Emotion companion chatbot.
 */
@Component
public class WhisperNest {

    private static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";

    private final ChatClient chatClient;

    @Resource
    private Advisor whisperNestCloudAdvisor;


    public WhisperNest(ChatModel dashscopeChatModel) {
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new FileBasedChatMemoryRepository(fileDir))
                .maxMessages(20)
                .build();
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(WhisperNestPrompt.SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    public Flux<String> doChatByStream(String msg, String chatId) {
        return chatClient.prompt()
                .user(msg)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .advisors(whisperNestCloudAdvisor)
                .stream()
                .content();
    }

    public String doChat(String msg, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(msg)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }
}
