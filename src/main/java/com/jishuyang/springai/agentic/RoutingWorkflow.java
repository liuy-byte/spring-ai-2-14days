package com.jishuyang.springai.agentic;

import org.springframework.ai.chat.client.ChatClient;

import java.util.Map;

/**
 * 模式②·Routing 路由：先判断输入类型，再分给对应的「专家人设」。
 * 客服最常用 —— 把问题分流到 账单/技术/通用 不同 system prompt。
 */
public class RoutingWorkflow {

    private final ChatClient chatClient;

    public RoutingWorkflow(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String route(String input, Map<String, String> routes) {
        // ① 先用一次调用判类型，只返回类型词
        String type = chatClient.prompt()
                .user("把下面的问题归类到这些类型之一：" + routes.keySet()
                        + "，只回类型词，不要任何多余内容。\n问题：" + input)
                .call()
                .content()
                .trim();

        // ② 按类型选对应专家人设，识别不出就兜底用第一个
        String systemPrompt = routes.getOrDefault(type, routes.values().iterator().next());

        // ③ 用选中的人设正式回答
        return chatClient.prompt()
                .system(systemPrompt)
                .user(input)
                .call()
                .content();
    }
}
