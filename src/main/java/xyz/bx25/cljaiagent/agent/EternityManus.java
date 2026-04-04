package xyz.bx25.cljaiagent.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;
import xyz.bx25.cljaiagent.advisor.MyLoggerAdvisor;

/**
 * Tool-enabled manuscript agent.
 */
@Component
public class EternityManus extends ToolCallAgent {

    public EternityManus(ToolCallback[] allTools, ChatModel dashscopeChatModel, ToolCallbackProvider toolCallbackProvider) {
        super(allTools, toolCallbackProvider);
        setName("EternityManus");

        String systemPrompt = """
                你是 EternityManus，一个能够调用工具解决复杂任务的中文智能助手。
                你的所有对外回复都必须使用自然、清晰的中文。
                除非用户明确要求，否则不要暴露你的内部思考链路、工具调用过程、工具名称或执行日志。
                当任务需要借助工具时，你可以自行调用工具完成，但面对用户时只输出对用户有用的结果、结论和必要说明。
                如果产出的是文件路径、图片路径或下载结果，优先用中文说明结果，不要把工具执行痕迹拼接进最终回答。
                """;
        setSystemPrompt(systemPrompt);

        String nextStepPrompt = """
                请根据用户需求主动选择最合适的工具或工具组合。
                如果任务复杂，可以分步调用工具解决。
                工具执行结果用于辅助你形成最终回答，不要把“tool xxx finished”之类的内部执行信息直接输出给用户。
                每一步思考都继续面向最终回答推进，不要重复空话。
                当你已经可以直接回答用户，或者任务已经完成时，不要继续调用工具。
                如果你决定结束任务，请调用 `terminate` 工具。
                """;
        setNextStepPrompt(nextStepPrompt);

        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        setChatClient(chatClient);
    }
}
