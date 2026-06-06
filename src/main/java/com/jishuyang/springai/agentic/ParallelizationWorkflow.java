package com.jishuyang.springai.agentic;

import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 模式③·Parallelization 并行：同一指令并发处理一批独立数据，再汇总。
 * 适合批量工单分类、多维度质检这类彼此不依赖的任务。
 */
public class ParallelizationWorkflow {

    private final ChatClient chatClient;

    public ParallelizationWorkflow(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public List<String> parallel(String instruction, List<String> inputs, int concurrency) {
        ExecutorService pool = Executors.newFixedThreadPool(concurrency);
        try {
            List<CompletableFuture<String>> futures = inputs.stream()
                    .map(input -> CompletableFuture.supplyAsync(
                            () -> chatClient.prompt()
                                    .user(instruction + "\n\n内容：" + input)
                                    .call()
                                    .content(),
                            pool))
                    .collect(Collectors.toList());
            // 等全部完成再按顺序收集结果
            return futures.stream().map(CompletableFuture::join).collect(Collectors.toList());
        } finally {
            pool.shutdown();
        }
    }
}
