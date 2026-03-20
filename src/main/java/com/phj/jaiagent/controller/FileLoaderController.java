package com.phj.jaiagent.controller;


import com.phj.jaiagent.rag.KnowledgeBaseLoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("")
public class FileLoaderController {
    @Autowired
    private KnowledgeBaseLoaderService knowledgeBaseLoaderService;

    @GetMapping("/init")
    public String Init(){
        try {
            knowledgeBaseLoaderService.loadKnowledgeBase();
            return "知识库加载完成";
        } catch (Exception e) {
            return "加载失败: " + e.getMessage();
        }
    }
}
