package xyz.bx25.cljaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author bx25 小陈
 * @Date 2026/3/31 11:39
 */
@SpringBootTest
class LoveAppDocumentLoaderTest {
    @Resource
    private LoveAppDocumentLoader loader;
    @Test
    void loadMarkdown() {
        loader.loadMarkdown();
    }
}