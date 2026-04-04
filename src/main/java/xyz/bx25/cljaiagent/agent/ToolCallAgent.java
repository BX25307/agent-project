package xyz.bx25.cljaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.agent.Agent;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientResponse;
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

import javax.tools.Tool;
import java.util.List;
import java.util.stream.Collectors;

/**
 *  bx25 小陈
 *  2026/4/3 18:45
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent{

    private final ToolCallback[] availableTools;

    private ChatResponse toolCallChatResponse;

    private final ToolCallingManager toolCallingManager;

    private final ToolCallbackProvider toolCallbackProvider;

    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools,ToolCallbackProvider toolCallbackProvider) {
        super();
        this.availableTools = availableTools;
        this.toolCallbackProvider=toolCallbackProvider;
        this.toolCallingManager=ToolCallingManager.builder().build();
        this.chatOptions= DashScopeChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();
    }
    @Override
    public boolean think() {
        if(getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()){
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, chatOptions);

        try {
            this.toolCallChatResponse= getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .toolCallbacks(toolCallbackProvider)
                    .call()
                    .chatResponse();
            AssistantMessage assistantMessage = toolCallChatResponse.getResult().getOutput();
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            log.info("{}的思考：{}", getName(), result);
            log.info("{}选择了：{} 个工具来使用", getName(), toolCallList.size());
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称:%s,参数:%s",
                            toolCall.name(),
                            toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);

            if(toolCallList.isEmpty()){
                getMessageList().add(assistantMessage);
                return false;
            }else{
                return true;
            }
        } catch (Exception e) {
            log.error("{}Error thinking facing problems{}", getName(), e.getMessage());
            getMessageList().add(
                    new AssistantMessage("Error thinking facing problems"+e.getMessage())
            );
            return false;
        }
    }

    @Override
    public String act() {
        if(!toolCallChatResponse.hasToolCalls()){
            return "no need to use tools";
        }

        Prompt prompt = new Prompt(getMessageList(), chatOptions);

        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);

        setMessageList(toolExecutionResult.conversationHistory());

        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());

        //是否调用了终止工具
        boolean doneTerminate = toolResponseMessage.getResponses().stream()
                .anyMatch(toolResponse -> toolResponse.name().equals("doTerminate"));
        if(doneTerminate){
            //任务结束
            setState(AgentState.FINISHED);
        }
        String results = toolResponseMessage.getResponses().stream()
                .map(toolResponse -> "工具 " + toolResponse.name() + " 完成了它的任务!")
                .collect(Collectors.joining("\n"));
        log.info(results);
        return results;
    }
}
