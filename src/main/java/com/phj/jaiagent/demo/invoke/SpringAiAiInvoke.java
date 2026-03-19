package com.phj.jaiagent.demo.invoke;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Spring AI 框架调用 AI 大模型（阿里）- 流式输出版
 */
//取消注释后，项目启动时会执行
//@Component
public class SpringAiAiInvoke implements CommandLineRunner {

    @Resource
    private ChatModel dashscopeChatModel;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("开始调用大模型 (流式输出)...");

        // 1. 将 call 替换为 stream
        dashscopeChatModel.stream(new Prompt("你好，我是PHJ"))
                .subscribe(
                        // 2. 正常接收到增量数据时：直接打印不换行
                        response -> {
                            String content = response.getResult().getOutput().getText();
                            if (content != null) {
                                System.out.print(content);
                            }
                        },
                        // 3. 发生错误时
                        error -> System.err.println("\n调用出错: " + error.getMessage()),
                        // 4. 全部输出完毕时
                        () -> System.out.println("\n\n--- 对话结束 ---")
                );
    }
}