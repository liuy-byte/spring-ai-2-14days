package com.jishuyang.springai.day08;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Day8：Embedding 与向量库。
 * 顺序：先 POST /day8/load 写入，再 GET /day8/search 检索。
 */
@RestController
@RequestMapping("/day8")
public class EmbeddingController {

    private final EmbeddingModel embeddingModel;
    private final VectorStore vectorStore;

    public EmbeddingController(EmbeddingModel embeddingModel, VectorStore vectorStore) {
        this.embeddingModel = embeddingModel;
        this.vectorStore = vectorStore;
    }

    /** 文本变向量 */
    @GetMapping("/embed")
    public Map<String, Object> embed(@RequestParam(defaultValue = "Spring AI 是什么") String text) {
        float[] vector = embeddingModel.embed(text);
        return Map.of("text", text, "dimensions", vector.length);
    }

    /** 写入几条文档 */
    @PostMapping("/load")
    public String load() {
        vectorStore.add(List.of(
                new Document("Spring AI 是 Spring 官方的 AI 应用开发框架"),
                new Document("DeepSeek 是国产大模型"),
                new Document("Docker 用于应用容器化部署")));
        return "已写入 3 条文档";
    }

    /** 语义检索 */
    @GetMapping("/search")
    public List<String> search(@RequestParam(defaultValue = "介绍一下 Spring AI") String query) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(query).topK(2).build());
        return docs.stream().map(Document::getText).toList();
    }
}
