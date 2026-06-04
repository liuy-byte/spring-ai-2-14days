package com.jishuyang.springai.day10;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day10：RAG 实战 —— QuestionAnswerAdvisor。
 * 先调 POST /day9/ingest 灌入知识，再问：
 *   /day10/ask?question=本系列的退货政策是几天？
 */
@RestController
@RequestMapping("/day10")
public class RagController {

    private final ChatClient chatClient;

    public RagController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build())
                .build();
    }

    @GetMapping("/ask")
    public String ask(@RequestParam(defaultValue = "Spring AI 2.0 有什么变化？") String question) {
        return chatClient.prompt().user(question).call().content();
    }
}
