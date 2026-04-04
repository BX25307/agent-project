package xyz.bx25.cljaiagent.agent;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;
import xyz.bx25.cljaiagent.advisor.MyLoggerAdvisor;

/**
 * Eternity智能体，拥有自主规划能力,可以直接使用
 *  bx25 小陈
 *  2026/4/3 18:45
 */
@Component
public class EternityManus extends ToolCallAgent {

    public EternityManus(ToolCallback[] allTools, ChatModel dashscopeChatModel,ToolCallbackProvider toolCallbackProvider) {
        super(allTools,toolCallbackProvider);
        setName("EternityManus");
        String SYSTEM_PROMPT = """
                You are EternityManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                """;
        setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                After using each tool, clearly explain the execution results and suggest the next steps.
                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                """;
        setNextStepPrompt(NEXT_STEP_PROMPT);

        //初始化客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        setChatClient(chatClient);

    }
}
