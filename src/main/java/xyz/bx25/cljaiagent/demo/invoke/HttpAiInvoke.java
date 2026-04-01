package xyz.bx25.cljaiagent.demo.invoke;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;

/**
 * Hutool HTTP 调用 Dashscope AI 文本生成接口示例
 */
public class HttpAiInvoke {

    // 你可以直接写死 Key 或者用环境变量
    private static final String API_KEY = TestApiKey.API_KEY; // 或 "sk-xxx"

    private static final String URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation";

    public static void main(String[] args) {
        // 这里直接传用户问题
        String userPrompt = "你是谁？";

        String answer = callAi(userPrompt);
        System.out.println("Assistant 回答: " + answer);
    }

    /**
     * 调用 Dashscope AI
     * @param userPrompt 用户输入的文本
     * @return AI 回复
     */
    public static String callAi(String userPrompt) {
        // 构建 JSON 请求体
        String jsonBody = """
        {
            "model": "qwen-plus",
            "input": {
                "messages": [
                    {
                        "role": "system",
                        "content": "You are a helpful assistant."
                    },
                    {
                        "role": "user",
                        "content": "%s"
                    }
                ]
            },
            "parameters": {
                "result_format": "message"
            }
        }
        """.formatted(userPrompt);

        try {
            HttpResponse response = HttpRequest.post(URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .body(jsonBody)
                    .execute();

            // 打印 HTTP 状态码和响应体，方便调试
            System.out.println("HTTP 响应码: " + response.getStatus());
            String body = response.body();
            System.out.println("响应体: " + body);

            if (body == null || body.isEmpty()) {
                return "返回为空，请检查 API Key 或网络";
            }

            JSONObject json = JSONUtil.parseObj(body);

            // 官方示例返回 messages 在 data 下
            if (json.containsKey("data")) {
                JSONObject data = json.getJSONObject("data");
                if (data.containsKey("messages")) {
                    JSONArray messages = data.getJSONArray("messages");
                    if (!messages.isEmpty()) {
                        JSONObject firstMsg = messages.getJSONObject(0);
                        return firstMsg.getStr("content");
                    }
                }
            }

            return "未解析到 AI 回复，请检查返回 JSON 结构";

        } catch (Exception e) {
            e.printStackTrace();
            return "调用接口异常: " + e.getMessage();
        }
    }
}