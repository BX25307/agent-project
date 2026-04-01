package xyz.bx25.cljaiagent.demo.invoke;

import dev.langchain4j.community.model.dashscope.QwenChatModel;

/**
 * @Author bx25 小陈
 * @Date 2026/3/29 13:39
 */
public class LangchainAiInvoke {
    public static void main(String[] args) {
        QwenChatModel chatModel = QwenChatModel.builder()
                .apiKey(TestApiKey.API_KEY)
                .modelName("qwen-plus")
                .build();
        String answer = chatModel.chat("你好呀，我是小金");
        System.out.println(answer);
    }
}
