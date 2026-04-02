package xyz.bx25.cljaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author bx25 小陈
 * @Date 2026/4/2 11:46
 */
@SpringBootTest
class WebSearchToolTest {
    @Value("${searchapi.api-key}")
    private String apiKey;
    @Test
    void webSearch() {
        WebSearchTool webSearchTool = new WebSearchTool(apiKey);
        String query="今天双鱼座的幸运色是什么";
        String res = webSearchTool.webSearch(query);
        Assertions.assertNotNull(res);
    }
}