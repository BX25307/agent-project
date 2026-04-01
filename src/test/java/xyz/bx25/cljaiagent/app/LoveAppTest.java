package xyz.bx25.cljaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import xyz.bx25.cljaiagent.CljAiAgentApplication;

import java.util.List;
import java.util.UUID;

@SpringBootTest(classes = CljAiAgentApplication.class)
class LoveAppTest {

    @Resource
    private LoveApp loveApp;

    @Test
    void shouldLoadLoveApp() {
        Assertions.assertNotNull(loveApp);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String msg="我单身，线上交友怎么才能快速脱单，你有什么推荐的课吗？";
        String res = loveApp.doChatWithRag(msg, chatId);
        Assertions.assertNotNull(res);
    }
}
