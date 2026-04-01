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
        String msg="今天学习好累，我应该吃什么呢";
        String res = loveApp.doChatWithRag(msg, chatId);
        Assertions.assertNotNull(res);
    }
}
