package xyz.bx25.cljaiagent.app;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import xyz.bx25.cljaiagent.advisor.MyLoggerAdvisor;
import xyz.bx25.cljaiagent.advisor.ReReadingAdvisor;
import xyz.bx25.cljaiagent.memory.FileBasedChatMemoryRepository;
import xyz.bx25.cljaiagent.rag.LoveAppCloudAdvisorConfig;
import xyz.bx25.cljaiagent.rag.LoveAppRagCustomAdvisorFactory;
import xyz.bx25.cljaiagent.rag.QueryRewriter;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Vector;

/**
 * @Author bx25 小陈
 * @Date 2026/3/30 14:06
 */
@Component
@Slf4j
public class LoveApp {
    private static final String CHAT_MEMORY_CONVERSATION_ID_KEY ="chat_memory_conversation_id" ;
    private static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY="chat_memory_retrieve_size";
    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。"+
            "不要太啰嗦，要简短还要有人气";

    /**
     * 初始化AI对话方法
     * @param dashscopeChatModel
     */
    public LoveApp(ChatModel dashscopeChatModel) {
        //初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";

        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new FileBasedChatMemoryRepository(fileDir))
                .maxMessages(20)
                .build();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                        ,new MyLoggerAdvisor()
//                        ,new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * AI对话方法(支持多轮对话)
     * @param msg
     * @param chatId
     * @return
     */
    public String doChat(String msg,String chatId){
        ChatResponse chatResponse = chatClient.prompt()
                .user(msg)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
//        log.info("content:{}",content);
        return content;
    }

    record LoveReport(String title, List<String> suggestions){

    }
    /**
     * AI 恋爱报告功能 实战结构化输出
     * @param msg
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String msg,String chatId){
        LoveReport loveReport = chatClient.prompt()
                .system(SYSTEM_PROMPT+"每次对话后都要生成恋爱结果,标题为{用户名}的恋爱报告，内容为建议列表")
                .user(msg)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport:{}",loveReport);
        return loveReport;
    }

    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private Advisor loveAppCloudAdvisor;

    @Resource
    private QueryRewriter queryRewriter;
    /**
     * 和RAG知识库对话
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message,String chatId){
//        String rewriterMessage = queryRewriter.doQueryRewriter(message);
        ChatResponse chatResponse = chatClient.prompt()
                //执行改写后的查询
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                //启用本地内存RAG知识库问答
//                .advisors(QuestionAnswerAdvisor.builder(loveAppVectorStore).build())
                //启用云知识库问答
//                .advisors(loveAppCloudAdvisor)
                //启用RAG检索增强(基于PgVector向量存储)
//                .advisors(QuestionAnswerAdvisor.builder(pgVectorVectorStore).build())
                //自定义增强检索
                .advisors(
                        LoveAppRagCustomAdvisorFactory.createRagCustomAdvisor(
                                loveAppVectorStore,"单身"
                        )
                )
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content:{}",content);
        return content;
    }

}
