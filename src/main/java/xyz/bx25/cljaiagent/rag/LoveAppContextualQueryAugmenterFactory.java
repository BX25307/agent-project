package xyz.bx25.cljaiagent.rag;

import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;

/**
 * @Author bx25 小陈
 * @Date 2026/4/1 20:02
 */
public class LoveAppContextualQueryAugmenterFactory {

    public static ContextualQueryAugmenter createInstance(){
        PromptTemplate emptyPromptTemplate = new PromptTemplate("""
                你应该输出下面的内容：
                抱歉，我只能回答感情相关的问题，别的没办法帮到您哦。
                有问题可以联系客服clj
                """);
        return ContextualQueryAugmenter.builder()
                .allowEmptyContext(false)
                .emptyContextPromptTemplate(emptyPromptTemplate)
                .build();
    }
}
