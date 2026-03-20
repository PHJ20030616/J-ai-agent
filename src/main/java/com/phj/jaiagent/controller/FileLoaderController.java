package com.phj.jaiagent.controller;


import com.phj.jaiagent.rag.KnowledgeBaseLoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/init")
public class FileLoaderController {
    @Autowired
    KnowledgeBaseLoaderService knowledgeBaseLoaderService;

    public void Init(){
        knowledgeBaseLoaderService.loadKnowledgeBase();
    }
}
