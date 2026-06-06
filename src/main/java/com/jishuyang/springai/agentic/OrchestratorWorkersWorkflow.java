package com.jishuyang.springai.agentic;

import org.springframework.ai.chat.client.ChatClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 模式④·Orchestrator-Workers：编排者动态把任务拆成子任务，工人并行处理，再合并。
 * 适合「事先不知道要分几步」的开放任务，比并行更灵活、代价也更高。
 */
public class OrchestratorWorkersWorkflow {

    private final ChatClient chatClient;

    public OrchestratorWorkersWorkflow(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /** 编排者拆出的子任务列表（结构化输出） */
    public record Subtasks(List<String> tasks) {
    }

    public String process(String task) {
        // ① 编排者：把大任务拆成若干子任务
        Subtasks subtasks = chatClient.prompt()
                .user("把下面的任务拆成 2-4 个可独立处理的子任务：\n" + task)
                .call()
                .entity(Subtasks.class);

        // ② 工人：并行处理每个子任务
        List<String> results = subtasks.tasks().stream()
                .map(sub -> CompletableFuture.supplyAsync(
                        () -> chatClient.prompt().user("完成子任务：" + sub).call().content()))
                .collect(Collectors.toList())   // 先全部提交，再统一 join，确保真并行
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        // ③ 合并：把子结果汇总成最终答复
        return chatClient.prompt()
                .user("把下面的子任务结果合并成一段完整、连贯的答复：\n" + String.join("\n---\n", results))
                .call()
                .content();
    }
}
