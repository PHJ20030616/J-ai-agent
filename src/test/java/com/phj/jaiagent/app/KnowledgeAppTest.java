package com.phj.jaiagent.app;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;


@SpringBootTest
class KnowledgeAppTest {

    @Autowired
    private KnowledgeApp knowledgeApp;

    //chatId使用UUID
    String chatId = UUID.randomUUID().toString();

    @Test
    void testDoChat() {


        String result = knowledgeApp.doChat("什么是缓存击穿？(简要回答)", chatId);
        result = knowledgeApp.doChat("什么是缓存雪崩?（简要回答）", chatId);
        result = knowledgeApp.doChat("我问过什么问题？", chatId);
    }

    @Test
    void testdoChatWithReport() {
        KnowledgeApp.KnowledgeReport knowledgeReport = knowledgeApp.doChatWithReport("什么是缓存击穿？(简要回答)", chatId);
        Assertions.assertNotNull(knowledgeReport);
    }

        @Test
        void testDoChatWithRag() {
            String result = knowledgeApp.doChatWithRag("什么是redis缓存击穿", chatId);
            Assertions.assertNotNull(result);
        }
}