package com.jishuyang.springai.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局共享 Bean：向量库、对话记忆。
 * 供 Day6、Day8-11 复用。
 */
@Configuration
public class AiConfig {

    /**
     * 内存向量库（学习用）。基于本地 Transformers 嵌入模型。
     * 生产环境换成 PgVector / Redis / Milvus 等，VectorStore 接口不变。
     */
    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    /**
     * 对话记忆：滑动窗口，只保留最近 20 条，防止 token 爆炸。
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }
}
