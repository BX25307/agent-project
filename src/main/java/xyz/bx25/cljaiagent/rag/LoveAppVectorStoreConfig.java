package xyz.bx25.cljaiagent.rag;

import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author bx25 小陈
 * @Date 2026/3/31 11:45
 */
@Configuration
public class LoveAppVectorStoreConfig {
    private static final int DASHSCOPE_EMBEDDING_BATCH_SIZE = 10;

    @Resource
    private LoveAppDocumentLoader loader;

    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel embeddingModel){
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        List<Document> documents = loader.loadMarkdown();
        for (int i = 0; i < documents.size(); i += DASHSCOPE_EMBEDDING_BATCH_SIZE) {
            int end = Math.min(i + DASHSCOPE_EMBEDDING_BATCH_SIZE, documents.size());
            simpleVectorStore.add(new ArrayList<>(documents.subList(i, end)));
        }
        return simpleVectorStore;
    }
}
