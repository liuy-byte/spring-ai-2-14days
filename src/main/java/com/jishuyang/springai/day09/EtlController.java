package com.jishuyang.springai.day09;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day9：触发 ETL，把示例 FAQ 文档灌入向量库。
 * 调用 POST /day9/ingest 后，可用 Day10 / Day11 的 RAG 接口提问。
 */
@RestController
@RequestMapping("/day9")
public class EtlController {

    private final EtlService etlService;

    public EtlController(EtlService etlService) {
        this.etlService = etlService;
    }

    @PostMapping("/ingest")
    public String ingest() {
        int n = etlService.ingest(new ClassPathResource("/docs/faq.txt"));
        return "已切分并入库 " + n + " 个文档块";
    }
}
