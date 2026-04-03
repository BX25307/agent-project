package xyz.bx25.cliimagesearchmcpserver.tool;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author bx25 小陈
 * @Date 2026/4/2 22:26
 */
@SpringBootTest
class ImageSearchToolTest {

    @Resource
    private ImageSearchTool imageSearchTool;
    @Test
    void imageSearch() {
        String res = imageSearchTool.imageSearch("a cute cat");
        System.out.println(res);
    }
}