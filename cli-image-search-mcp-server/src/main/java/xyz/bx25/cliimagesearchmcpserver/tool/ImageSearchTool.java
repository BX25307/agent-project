package xyz.bx25.cliimagesearchmcpserver.tool;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ImageSearchTool {
    @Value("${pexels.api-key}")
    private String pexelsApiKey;

    private static final String PEXELS_SEARCH_URL = "https://api.pexels.com/v1/search";

    @Tool(description = "Search image from web")
    public String imageSearch(@ToolParam(description = "search query keyword") String searchTopic) {
        try {
            HttpResponse response = HttpRequest.get(PEXELS_SEARCH_URL)
                    .header("Authorization", pexelsApiKey)
                    .form("query", searchTopic)
                    .form("per_page", 1)
                    .timeout(10_000)
                    .execute();

            if (response.getStatus() != 200) {
                return "Image search failed, HTTP status: " + response.getStatus() + ", response: " + response.body();
            }

            JSONObject jsonObject = JSONUtil.parseObj(response.body());
            JSONArray photos = jsonObject.getJSONArray("photos");

            if (photos == null || photos.isEmpty()) {
                return "No image found for [" + searchTopic + "].";
            }

            JSONObject firstPhoto = photos.getJSONObject(0);
            JSONObject src = firstPhoto.getJSONObject("src");

            String imageUrl = src.getStr("medium");
            String photographer = firstPhoto.getStr("photographer");
            String alt = firstPhoto.getStr("alt");
            String pexelsPageUrl = firstPhoto.getStr("url");

            return "Image search succeeded%nKeyword: %s%nDescription: %s%nPhotographer: %s%nImage URL: %s%nPexels Page: %s"
                    .formatted(searchTopic, alt, photographer, imageUrl, pexelsPageUrl);
        } catch (Exception e) {
            return "Pexels image search error: " + e.getMessage();
        }
    }
}
