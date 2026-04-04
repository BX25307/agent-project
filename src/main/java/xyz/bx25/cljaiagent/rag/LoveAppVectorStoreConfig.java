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
//@Configuration
public class LoveAppVectorStoreConfig {
    private static final int DASHSCOPE_EMBEDDING_BATCH_SIZE = 10;

    @Resource
    private LoveAppDocumentLoader loader;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;
//    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel embeddingModel){
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(embeddingModel).build();
        List<Document> documents = loader.loadMarkdown();
        //自动补充关键词元信息
        List<Document> enricherDocuments = myKeywordEnricher.enricherDocuments(documents);
        simpleVectorStore.add(enricherDocuments);
        return simpleVectorStore;
    }
}
