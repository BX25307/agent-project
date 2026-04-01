package xyz.bx25.cljaiagent.prompt;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class MyPromptTemplateTest {
    @Resource
    private MyPromptTemplate template;
    @Test
    void shouldRenderTemplateFromClasspathResource() {

        String content = template.render(Map.of(
                "role", "恋爱顾问",
                "user_name", "小金",
                "scenario", "异地恋沟通",
                "question", "最近总因为回复消息慢吵架怎么办？",
                "tone", "温和、专业",
                "output_format", "分点列表"
        ));

        assertTrue(content.contains("你现在的角色是：恋爱顾问"));
        assertTrue(content.contains("服务对象：小金"));
        assertTrue(content.contains("场景背景：异地恋沟通"));
        assertTrue(content.contains("用户问题："));
        assertTrue(content.contains("最近总因为回复消息慢吵架怎么办？"));
        assertTrue(content.contains("回答风格保持 温和、专业"));
        assertTrue(content.contains("输出格式：分点列表"));

    }
}
