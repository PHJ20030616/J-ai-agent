package com.phj.jaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AI开发者智能助手-文档加载器
 */
@Component
@Slf4j
public class KnowledgeAppDocumentLoader {

    private final ResourcePatternResolver resourcePatternResolver;

    @Value("${spring.document.path}")
    private String documentPath;

    public KnowledgeAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载多篇 pdf 文档
     *
     * @return
     */
    List<Document> loadText() {
        try {
            Resource[] resources = resourcePatternResolver.getResources(documentPath);
            List<Document> documents = new ArrayList<>();
            for (Resource resource : resources) {
                TikaDocumentReader tikaDocumentReader = new TikaDocumentReader(resource);
                documents.addAll(tikaDocumentReader.read());
            }
            return documents;
        } catch (IOException e) {
            log.error("加载PDF文档失败", e);
            return Collections.emptyList();
        }
    }
}
