//package com.phj.jaiagent.rag;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.ai.document.Document;
//
//import java.util.List;
//
//@SpringBootTest
//class KnowledgeAppDocumentLoaderTest {
//
//    @Autowired
//    private KnowledgeAppDocumentLoader knowledgeAppDocumentLoader;
//
//    @Test
//    void loadText() {
//        List<Document> documents = knowledgeAppDocumentLoader.loadText();
//        Assertions.assertNotNull(documents);
//        System.out.println("加载文档数量: " + documents.size());
//        documents.forEach(doc -> System.out.println("文档内容: " + doc.getText().substring(0, Math.min(100, doc.getText().length()))));
//    }
//}