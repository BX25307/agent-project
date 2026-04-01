package xyz.bx25.cljaiagent.rag;

import com.alibaba.cloud.ai.dashscope.spec.DashScopeModel;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * @Author bx25 小陈
 * @Date 2026/3/31 11:45
 */
@Configuration
public class LoveAppVectorStoreConfig {
    @Resource
    private LoveAppDocumentLoader loader;
    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel embeddingModel){
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        List<Document> documents = loader.loadMarkdown();
        simpleVectorStore.add(documents);
        return simpleVectorStore;
    }
}
