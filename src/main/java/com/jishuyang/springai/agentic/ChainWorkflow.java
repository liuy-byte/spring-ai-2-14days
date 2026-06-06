package com.jishuyang.springai.agentic;

import org.springframework.ai.chat.client.ChatClient;

/**
 * 模式①·Chain 链式：把复杂任务拆成顺序步骤，每步的输出喂给下一步。
 * 适合步骤固定、前后依赖的任务，用延迟换准确率。
 */
public class ChainWorkflow {

    private final ChatClient chatClient;
    private final String[] systemPrompts;

    public ChainWorkflow(ChatClient chatClient, String... systemPrompts) {
        this.chatClient = chatClient;
        this.systemPrompts = systemPrompts;
    }

    public String chain(String userInput) {
        String response = userInput;
        for (String prompt : systemPrompts) {
            // 把「本步指令 + 上一步结果」拼成输入，交给模型
            String input = prompt + "\n\n上一步结果：\n" + response;
            response = chatClient.prompt().user(input).call().content();
        }
        return response;
    }
}
