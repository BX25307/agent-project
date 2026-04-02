package xyz.bx25.cljaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author bx25 小陈
 * @Date 2026/4/2 11:11
 */
class FileOperationToolTest {

    @Test
    void readFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "test";
        String res = fileOperationTool.readFile(fileName);
        Assertions.assertNotNull(res);
    }

    @Test
    void writeFile() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        String fileName = "test";
        String content = "hello world";
        String res = fileOperationTool.writeFile(fileName, content);
        Assertions.assertNotNull(res);

    }
}