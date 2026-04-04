package xyz.bx25.cljaiagent.controller;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;
import xyz.bx25.cljaiagent.agent.EternityManus;
import xyz.bx25.cljaiagent.app.LoveApp;
import xyz.bx25.cljaiagent.app.WhisperNest;

/**
 * HTTP endpoints for AI chat apps and agents.
 */
@RequestMapping("/ai")
@RestController
public class AiAgentController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private WhisperNest whisperNest;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * Returns a synchronous LoveApp response.
     */
    @GetMapping("love_app.chat/sync")
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    @GetMapping(value = "/love_app/chat/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSse(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId);
    }

    @GetMapping("/lova_app/chat/server_sent_event")
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    @GetMapping("/love_app/chat/emitter")
    public SseEmitter doChatWithLoveAppEmitter(String message, String chatId) {
        SseEmitter sseEmitter = new SseEmitter(180000L);

        loveApp.doChatByStream(message, chatId)
                .subscribe(
                        chunk -> {
                            try {
                                sseEmitter.send(chunk);
                            } catch (Exception e) {
                                sseEmitter.completeWithError(e);
                            }
                        },
                        sseEmitter::completeWithError,
                        sseEmitter::complete
                );
        return sseEmitter;
    }

    @GetMapping("/whisper_nest/chat/emitter")
    public SseEmitter doChatWithWhisperNestEmitter(String message, String chatId) {
        SseEmitter sseEmitter = new SseEmitter(180000L);

        whisperNest.doChatByStream(message, chatId)
                .subscribe(
                        chunk -> {
                            try {
                                sseEmitter.send(chunk);
                            } catch (Exception e) {
                                sseEmitter.completeWithError(e);
                            }
                        },
                        sseEmitter::completeWithError,
                        sseEmitter::complete
                );
        return sseEmitter;
    }

    /**
     * Streams EternityManus execution steps.
     */
    @GetMapping("/eternity_manus/emitter")
    public SseEmitter doManusWithStream(String message) {
        EternityManus manus = new EternityManus(allTools, dashscopeChatModel, toolCallbackProvider);
        return manus.runByStream(message);
    }
}
