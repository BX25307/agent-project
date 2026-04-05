package xyz.bx25.cljaiagent.agent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import xyz.bx25.cljaiagent.agent.model.AgentState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 *  bx25 小陈
 *  2026/4/3 18:45
 */
@Data
@Slf4j
public abstract class BaseAgent {
    //名字
    private String name;

    //提示词
    private String SystemPrompt;
    private String nextStepPrompt;

    //agent状态
    private AgentState state=AgentState.IDLE;

    //执行步数
    private int maxStep= 20;
    private int currentStep=0;

    //llm大模型
    private ChatClient chatClient;

    //记忆上下文
    private List<Message> messageList=new ArrayList<>();

    /**
     * 执行逻辑,同步调用
     */
    public String run(String userPrompt){
        if(this.state!=AgentState.IDLE){
            throw new RuntimeException("Cannot run agent from state:"+this.state);
        }
        if(StringUtils.isBlank(userPrompt)){
            throw new RuntimeException("user prompt cannot be blank");
        }

        state=AgentState.RUNNING;

        messageList.add(new UserMessage(userPrompt));

        List<String> results=new ArrayList<>();

        try {
            for(int i=0;i<maxStep && state !=AgentState.FINISHED;i++){
                currentStep++;
                log.info("Executing step "+currentStep+"/"+maxStep);

                String stepRes = step();
                String res = "Step " + currentStep + ": " + stepRes;
                results.add(res);
            }

            if(currentStep>=maxStep){
                state=AgentState.FINISHED;
                results.add("Terminated: Reached max step ("+maxStep+")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state=AgentState.ERROR;
            log.error("Error executing agent",e);
            return "Error executing agent: "+e.getMessage();
        } finally {
            this.cleanUp();
        }
    }

    /**
     * 执行逻辑,SSE emitter异步调用
     */
    public SseEmitter runByStream(String userPrompt) {
        SseEmitter sseEmitter = new SseEmitter(300000L);
        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    sseEmitter.send("Cannot run agent from state:" + this.state);
                    sseEmitter.complete();
                    return;
                }
                if (StringUtils.isBlank(userPrompt)) {
                    sseEmitter.send("user prompt cannot be blank");
                    sseEmitter.complete();
                    return;
                }
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }

            state = AgentState.RUNNING;

            messageList.add(new UserMessage(userPrompt));

            try {
                for (int i = 0; i < maxStep && state != AgentState.FINISHED; i++) {
                    currentStep++;
                    log.info("Executing step " + currentStep + "/" + maxStep);

                    String stepRes = step();
                    if (StringUtils.isNotBlank(stepRes)) {
                        sseEmitter.send(stepRes);
                    }
                }

                if (currentStep >= maxStep) {
                    state = AgentState.FINISHED;
                    sseEmitter.send("Terminated: Reached max step (" + maxStep + ")");
                }
                sseEmitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("Error executing agent", e);
                try {
                    sseEmitter.send("Error executing agent" + e.getMessage());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                this.cleanUp();
            }
        });

        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanUp();
            log.warn("SSE connection timed out");
        });

        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanUp();
            log.info("MCP connection completed");
        });

        return sseEmitter;
    }

    /**
     * 具体怎么执行，交给子类完成
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanUp(){
        //子类可以实现
    }
}
