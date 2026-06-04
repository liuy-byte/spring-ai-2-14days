package com.jishuyang.springai.day11;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day11：模块化 RAG —— RetrievalAugmentationAdvisor。
 * 含查询改写 + 可调阈值检索 + 空上下文兜底。
 * 同样先调 POST /day9/ingest 灌入知识。
 */
@RestController
@RequestMapping("/day11")
public class ModularRagController {

    private final ChatClient chatClient;
    private final Advisor ragAdvisor;

    public ModularRagController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.ragAdvisor = RetrievalAugmentationAdvisor.builder()
                // 检索前用大模型改写口语问题，提升召回
                .queryTransformers(RewriteQueryTransformer.builder()
                        .chatClientBuilder(builder.build().mutate())
                        .build())
                // 可调相似度阈值的文档召回
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .similarityThreshold(0.5)
                        .vectorStore(vectorStore)
                        .build())
                // 检索为空也让模型作答，不直接拒答
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        .allowEmptyContext(true)
                        .build())
                .build();
        this.chatClient = builder.build();
    }

    @GetMapping("/ask")
    public String ask(@RequestParam(defaultValue = "Spring AI 2.0 主要有哪些变化？") String question) {
        return chatClient.prompt()
                .advisors(ragAdvisor)
                .user(question)
                .call()
                .content();
    }
}
