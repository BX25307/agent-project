package xyz.bx25.cljaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;

/**
 * 终止工具
 * bx25 小陈
 * 2026/4/3 21:02
 */
public class TerminateTool {

    @Tool(description = """
            Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.
            "When you have finished all the tasks, call this tool to end the work.
            """)
    public String doTerminate() {
        return "The task is done";
    }
}
