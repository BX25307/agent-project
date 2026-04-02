package xyz.bx25.cljaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @Author bx25 小陈
 * @Date 2026/4/2 12:22
 */
class TerminalOperationToolTest {

    @Test
    void executeTerminalCommand() {
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        String res = terminalOperationTool.executeTerminalCommand("dir");
        Assertions.assertNotNull(res);
    }
}