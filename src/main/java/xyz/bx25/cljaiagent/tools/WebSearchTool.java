package xyz.bx25.cljaiagent.tools;

import cn.hutool.http.HttpUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * 网页搜索工具类
 * @Author bx25 小陈
 * @Date 2026/4/2 11:38
 */
public class WebSearchTool {

    private final String API_KEY;
    private static final String SEARCH_URL = "https://serpapi.com/search.json";

    public WebSearchTool(String apiKey) {
        API_KEY=apiKey;
    }
    @Tool(description = "Search for information from Baidu Search Engine")
    public String webSearch(@ToolParam(description = "Search query keyword") String query){
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("engine", "baidu");
            paramMap.put("q", query);
            paramMap.put("api_key", API_KEY);
            return HttpUtil.get(SEARCH_URL, paramMap);
        } catch (Exception e) {
            return "Error searching Baidu Search Engine"+e.getMessage();
        }
    }
}
