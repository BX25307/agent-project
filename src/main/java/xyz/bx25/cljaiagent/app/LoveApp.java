package xyz.bx25.cljaiagent.app;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import xyz.bx25.cljaiagent.advisor.MyLoggerAdvisor;
import xyz.bx25.cljaiagent.memory.FileBasedChatMemoryRepository;
import xyz.bx25.cljaiagent.prompt.LoveAppPrompt;
import xyz.bx25.cljaiagent.rag.QueryRewriter;

import java.util.List;

/**
 * Love relationship companion app.
 */
@Component
@Slf4j
public class LoveApp {

    private static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    private static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_retrieve_size";

    private final ChatClient chatClient;

    /**
     * Initializes the chat client and conversation memory.
     *
     * @param dashscopeChatModel chat model
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new FileBasedChatMemoryRepository(fileDir))
                .maxMessages(20)
                .build();
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(LoveAppPrompt.SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        new MyLoggerAdvisor()
                )
                .build();
    }

    /**
     * Streams chat content for multi-turn conversations.
     */
    public Flux<String> doChatByStream(String msg, String chatId) {
        return chatClient.prompt()
                .user(msg)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .advisors(loveAppCloudAdvisor)
                .stream()
                .content();
    }

    /**
     * Returns a single chat response for multi-turn conversations.
     */
    public String doChat(String msg, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(msg)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .call()
                .chatResponse();
        return chatResponse.getResult().getOutput().getText();
    }

    record LoveReport(String title, List<String> suggestions) {
    }

    /**
     * Generates a structured love report.
     */
    public LoveReport doChatWithReport(String msg, String chatId) {
        LoveReport loveReport = chatClient.prompt()
                .system(LoveAppPrompt.SYSTEM_PROMPT + LoveAppPrompt.REPORT_SUFFIX_PROMPT)
                .user(msg)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport:{}", loveReport);
        return loveReport;
    }

//    @Resource
//    private VectorStore loveAppVectorStore;

    @Resource
    private Advisor loveAppCloudAdvisor;

//    @Resource
//    private QueryRewriter queryRewriter;

    /**
     * Chats with retrieval augmentation support.
     */
    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content:{}", content);
        return content;
    }

    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String msg, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(msg)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content:{}", content);
        return content;
    }

    /**
     * Calls MCP-enabled tools during chat.
     */
    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    public String doChatWithMCP(String msg, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .user(msg)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbackProvider)
                .toolCallbacks(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content:{}", content);
        return content;
    }
}
