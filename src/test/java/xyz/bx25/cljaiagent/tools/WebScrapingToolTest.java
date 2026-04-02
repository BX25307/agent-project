package xyz.bx25.cljaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author bx25 小陈
 * @Date 2026/4/2 12:10
 */
class WebScrapingToolTest {

    @Test
    void scrapeWebPage() {
        WebScrapingTool t = new WebScrapingTool();
        String res = t.scrapeWebPage("https://www.codefather.cn");
        Assertions.assertNotNull(res);
    }
}