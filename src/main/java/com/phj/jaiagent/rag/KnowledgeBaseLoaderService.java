package com.phj.jaiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 知识库手动加载服务
 * 支持按需加载文档到向量数据库，避免程序启动时自动加载
 */
@Service
@Slf4j
public class KnowledgeBaseLoaderService {

    @Resource
    private KnowledgeAppDocumentLoader knowledgeAppDocumentLoader;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Resource
    private MyKeywordEnricher myKeywordEnricher;

    @Resource
    private VectorStore vectorStore;

    /**
     * 手动加载知识库文档到向量数据库
     * 调用此方法会重新加载并切分所有文档，然后添加到向量存储
     */
    public void loadKnowledgeBase() {
        log.info("开始加载知识库文档...");
        long startTime = System.currentTimeMillis();

        List<Document> documents = knowledgeAppDocumentLoader.loadText();
        log.info("加载文档数量: {}", documents.size());

        List<Document> splitDocuments = myTokenTextSplitter.splitCustomized(documents);
        log.info("切分后文档数量: {}", splitDocuments.size());

        List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(splitDocuments);

        int batchSize = 25;
        for (int i = 0; i < enrichedDocuments.size(); i += batchSize) {
//            log.info("正在添加文档批次: {}/{}", (i / batchSize) + 1, (enrichedDocuments.size() + batchSize - 1) / batchSize);
            int end = Math.min(i + batchSize, enrichedDocuments.size());
            List<Document> batch = enrichedDocuments.subList(i, end);

            vectorStore.add(batch);
            log.info("已添加文档批次: {}/{}", (i / batchSize) + 1, (enrichedDocuments.size() + batchSize - 1) / batchSize);
        }

        long endTime = System.currentTimeMillis();
        log.info("知识库加载完成，共添加 {} 条文档，耗时 {} ms", enrichedDocuments.size(), endTime - startTime);
    }
}
