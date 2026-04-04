package xyz.bx25.cljaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import xyz.bx25.cljaiagent.agent.model.AgentState;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Tool-calling agent implementation.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    private final ToolCallback[] availableTools;

    private ChatResponse toolCallChatResponse;

    private String latestAssistantReply;

    private final ToolCallingManager toolCallingManager;

    private final ToolCallbackProvider toolCallbackProvider;

    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools, ToolCallbackProvider toolCallbackProvider) {
        super();
        this.availableTools = availableTools;
        this.toolCallbackProvider = toolCallbackProvider;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();
    }

    @Override
    public boolean think() {
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            getMessageList().add(new UserMessage(getNextStepPrompt()));
        }

        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, chatOptions);

        try {
            this.toolCallChatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .toolCallbacks(toolCallbackProvider)
                    .call()
                    .chatResponse();

            AssistantMessage assistantMessage = toolCallChatResponse.getResult().getOutput();
            this.latestAssistantReply = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();

            log.info("{} thinking: {}", getName(), latestAssistantReply);
            log.info("{} selected {} tools", getName(), toolCallList.size());

            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("tool: %s, args: %s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            if (!toolCallInfo.isBlank()) {
                log.info(toolCallInfo);
            }

            if (toolCallList.isEmpty()) {
                getMessageList().add(assistantMessage);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("{} error while thinking: {}", getName(), e.getMessage(), e);
            this.latestAssistantReply = "Error thinking facing problems" + e.getMessage();
            getMessageList().add(new AssistantMessage(latestAssistantReply));
            return false;
        }
    }

    @Override
    protected String onThinkCompletedWithoutAct() {
        return latestAssistantReply;
    }

    @Override
    protected String onThinkCompletedBeforeAct() {
        return latestAssistantReply;
    }

    @Override
    protected String onActCompleted() {
        act();
        return null;
    }

    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "no need to use tools";
        }

        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        setMessageList(toolExecutionResult.conversationHistory());

        ToolResponseMessage toolResponseMessage =
                (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());

        boolean doneTerminate = toolResponseMessage.getResponses().stream()
                .anyMatch(toolResponse -> toolResponse.name().equals("doTerminate"));
        if (doneTerminate) {
            setState(AgentState.FINISHED);
        }

        String results = toolResponseMessage.getResponses().stream()
                .map(toolResponse -> "tool " + toolResponse.name() + " finished its task")
                .collect(Collectors.joining("\n"));
        log.info(results);
        return results;
    }
}
