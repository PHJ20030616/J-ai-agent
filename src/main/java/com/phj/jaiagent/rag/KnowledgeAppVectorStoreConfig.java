package com.phj.jaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 向量数据库配置（初始化基于内存的向量数据库 Bean）
 */
//@Configuration
public class KnowledgeAppVectorStoreConfig {

    @Resource
    private KnowledgeAppDocumentLoader knowledgeAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Bean
    VectorStore knowledgeAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        // 加载文档
        List<Document> documentList = knowledgeAppDocumentLoader.loadText();
        // 自主切分文档
        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documentList);
        // 自动补充关键词元信息
        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(splitDocuments);
        // 判断增强后的文档列表是否不为空且包含元素，避免抛出 Documents list cannot be empty 异常
        if (enrichedDocuments != null && !enrichedDocuments.isEmpty()) {
            // 将增强后的文档列表添加到向量数据库中
            simpleVectorStore.add(enrichedDocuments);
        // 结束if条件判断
        }
//        simpleVectorStore.add(splitDocuments);
        return simpleVectorStore;
    }
}
