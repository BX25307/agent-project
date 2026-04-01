package xyz.bx25.cljaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于AI的文档元信息收集器(为文档补充元信息)
 * @Author bx25 小陈
 * @Date 2026/4/1 18:07
 */
@Component
public class MyKeywordEnricher {

    @Resource
    private ChatModel dashscopeChatModel;
    public List<Document> enricherDocuments(List<Document> documents){
        KeywordMetadataEnricher keywordMetadataEnricher = new KeywordMetadataEnricher(dashscopeChatModel, 5);
        return keywordMetadataEnricher.apply(documents);
    }
}
