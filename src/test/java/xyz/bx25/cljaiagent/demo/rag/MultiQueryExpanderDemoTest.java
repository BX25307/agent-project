package xyz.bx25.cljaiagent.demo.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author bx25 小陈
 * @Date 2026/4/1 18:44
 */
@SpringBootTest
class MultiQueryExpanderDemoTest {
    @Resource
    private MultiQueryExpanderDemo multiQueryExpanderDemo;
    @Test
    void expand() {
        List<Query> queryList = multiQueryExpanderDemo.expand("我喜欢狗吗");
        Assertions.assertNotNull(queryList);
    }
}