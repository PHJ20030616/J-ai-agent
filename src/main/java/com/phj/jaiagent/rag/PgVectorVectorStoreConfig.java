package com.phj.jaiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

// 为方便开发调试和部署，临时注释，如果需要使用 PgVector 存储知识库，取消注释即可
@Configuration
@Slf4j
public class PgVectorVectorStoreConfig {


    @Resource
    private KnowledgeAppDocumentLoader knowledgeAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Bean
    public VectorStore pgVectorVectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        log.info("使用pgVector数据库");
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1536)                    // Optional: defaults to model dimensions or 1536
                .distanceType(COSINE_DISTANCE)       // Optional: defaults to COSINE_DISTANCE
                .indexType(HNSW)                     // Optional: defaults to HNSW
                .initializeSchema(true)              // Optional: defaults to false
                .schemaName("public")                // Optional: defaults to "public"
                .vectorTableName("vector_store")     // Optional: defaults to "vector_store"
                .maxDocumentBatchSize(10000)         // Optional: defaults to 10000
                .build();
//        // 加载文档
//        List<Document> documents = knowledgeAppDocumentLoader.loadText();
        // 使用 TokenTextSplitter 将大文档切分为小块，避免超过 token 限制
//        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documents);

        // 分批添加文档，避免超过 DashScope API 的 25 条文本限制
//        int batchSize = 25;
//        for (int i = 0; i < splitDocuments.size(); i += batchSize) {
//            int end = Math.min(i + batchSize, splitDocuments.size());
//            List<Document> batch = splitDocuments.subList(i, end);
//            vectorStore.add(batch);
//        }
        
        return vectorStore;
    }

}
