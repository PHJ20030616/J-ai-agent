package com.phj.jaiagent.app;

import com.phj.jaiagent.advisor.MyLoggerAdvisor;
import com.phj.jaiagent.advisor.ReReadingAdvisor;
import com.phj.jaiagent.chatmemory.FileBasedChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class KnowledgeApp {

//    private static final Logger log = LoggerFactory.getLogger(KnowledgeApp.class);

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT = "# Role\n" +
            "你是一个经验丰富的“资深开发大师”和“技术导师”。你不仅精通各类技术栈、系统架构和最佳实践，还深谙程序员的职业发展路径。\n" +
            "\n" +
            "# Objective\n" +
            "你的目标是解答开发者的技术问题，帮助他们不仅“知其然”，还能“知其所以然”，通过拓展核心技术知识，助力他们突破技术瓶颈，实现职业进阶。\n" +
            "\n" +
            "# Guidelines\n" +
            "1. **精准解答**：首先直接、准确地回答用户的当前问题。如果涉及代码，请提供清晰、有注释且符合最佳实践的代码示例。\n" +
            "2. **深度拓展**：在解决问题的基础上，主动剖析底层的原理、可能遇到的陷阱（Pitfalls）、性能优化建议或相关的系统设计理念。\n" +
            "3. **授人以渔**：适时引入工程化思维，教导用户如何调试、如何阅读源码或如何进行技术选型。\n" +
            "4. **结构化输出**：使用清晰的标题、列表和代码块（Markdown）来组织你的回答，确保内容易读且重点突出。\n" +
            "5. **启发式引导**：在每次回答的最后，**必须**以提问的方式收尾。提出一个与当前话题紧密相关、能引发更深层次思考的具体技术问题，询问用户是否需要继续探讨。\n" +
            "\n" +
            "# Tone\n" +
            "专业、严谨、有耐心且富有启发性。语气要像一位乐于分享的资深同事，既有技术极客的严谨，又有导师的温暖。\n" +
            "\n" +
            "# Output Format Example\n" +
            "- **核心解答**：[直接给出解决方案或代码]\n" +
            "- **原理剖析/最佳实践**：[拓展相关的底层知识或高级技巧]\n" +
            "- **下一步探索**：[用一个具体的启发式问题结尾，例如：“关于这个问题，其实还涉及到高并发下的锁机制，你想进一步了解如何通过分布式锁来优化吗？”]";

    /**
     * 初始化 ChatClient
     *
     * @param dashscopeChatModel
     */
    public KnowledgeApp(ChatModel dashscopeChatModel) {
        // 初始化基于文件的对话记忆
        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
        // 初始化基于内存的对话记忆
//        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
//                .chatMemoryRepository(new InMemoryChatMemoryRepository())
//                .maxMessages(20)
//                .build();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor()
//                        // 自定义推理增强 Advisor，可按需开启
//                       ,new ReReadingAdvisor()
                )
                .build();
    }

    /**
     * AI 基础对话（支持多轮对话记忆）
     *
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

//    /**
//     * AI 基础对话（支持多轮对话记忆，SSE 流式传输）
//     *
//     * @param message
//     * @param chatId
//     * @return
//     */
//    public Flux<String> doChatByStream(String message, String chatId) {
//        return chatClient
//                .prompt()
//                .user(message)
//                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
//                .stream()
//                .content();
//    }
//
    // AI 技术报告类
    record KnowledgeReport(String title, List<String> suggestions) {

    }

    /**
     * 技术报告功能（实战结构化输出）
     *
     * @param message
     * @param chatId
     * @return
     */
    public KnowledgeReport doChatWithReport(String message, String chatId) {
        KnowledgeReport knowledgeReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话后都要生成技术报告，标题为{用户名}的技术报告，内容为相关技术知识")
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(KnowledgeReport.class);
        log.info("knowledgeReport: {}", knowledgeReport);
        return knowledgeReport;
    }
//
//    // AI 恋爱知识库问答功能
//
//    @Resource
//    private VectorStore loveAppVectorStore;
//
//    @Resource
//    private Advisor loveAppRagCloudAdvisor;
//
//    @Resource
//    private VectorStore pgVectorVectorStore;
//
//    @Resource
//    private QueryRewriter queryRewriter;
//
//    /**
//     * 和 RAG 知识库进行对话
//     *
//     * @param message
//     * @param chatId
//     * @return
//     */
//    public String doChatWithRag(String message, String chatId) {
//        // 查询重写
//        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
//        ChatResponse chatResponse = chatClient
//                .prompt()
//                // 使用改写后的查询
//                .user(rewrittenMessage)
//                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
//                // 开启日志，便于观察效果
//                .advisors(new MyLoggerAdvisor())
//                // 应用 RAG 知识库问答
//                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
//                // 应用 RAG 检索增强服务（基于云知识库服务）
////                .advisors(loveAppRagCloudAdvisor)
//                // 应用 RAG 检索增强服务（基于 PgVector 向量存储）
////                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
//                // 应用自定义的 RAG 检索增强服务（文档查询器 + 上下文增强器）
////                .advisors(
////                        LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(
////                                loveAppVectorStore, "单身"
////                        )
////                )
//                .call()
//                .chatResponse();
//        String content = chatResponse.getResult().getOutput().getText();
//        log.info("content: {}", content);
//        return content;
//    }
//
//    // AI 调用工具能力
//    @Resource
//    private ToolCallback[] allTools;
//
//    /**
//     * AI 恋爱报告功能（支持调用工具）
//     *
//     * @param message
//     * @param chatId
//     * @return
//     */
//    public String doChatWithTools(String message, String chatId) {
//        ChatResponse chatResponse = chatClient
//                .prompt()
//                .user(message)
//                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
//                // 开启日志，便于观察效果
//                .advisors(new MyLoggerAdvisor())
//                .toolCallbacks(allTools)
//                .call()
//                .chatResponse();
//        String content = chatResponse.getResult().getOutput().getText();
//        log.info("content: {}", content);
//        return content;
//    }
//
//    // AI 调用 MCP 服务
//
//    @Resource
//    private ToolCallbackProvider toolCallbackProvider;
//
//    /**
//     * AI 恋爱报告功能（调用 MCP 服务）
//     *
//     * @param message
//     * @param chatId
//     * @return
//     */
//    public String doChatWithMcp(String message, String chatId) {
//        ChatResponse chatResponse = chatClient
//                .prompt()
//                .user(message)
//                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
//                // 开启日志，便于观察效果
//                .advisors(new MyLoggerAdvisor())
//                .toolCallbacks(toolCallbackProvider)
//                .call()
//                .chatResponse();
//        String content = chatResponse.getResult().getOutput().getText();
//        log.info("content: {}", content);
//        return content;
//    }
}
