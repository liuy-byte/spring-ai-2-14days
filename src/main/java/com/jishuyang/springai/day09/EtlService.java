package com.jishuyang.springai.day09;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Day9：文档 ETL —— 读取 → 切块 → 写入向量库。
 */
@Service
public class EtlService {

    private final VectorStore vectorStore;

    public EtlService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public int ingest(Resource resource) {
        // Extract：Tika 读各种格式（PDF/Word/PPT/HTML/TXT）
        List<Document> docs = new TikaDocumentReader(resource).read();

        // Transform：按 token 切块
        List<Document> chunks = TokenTextSplitter.builder()
                .withChunkSize(800)
                .build()
                .apply(docs);

        // Load：写入向量库（内部自动向量化）
        vectorStore.accept(chunks);

        return chunks.size();
    }
}
