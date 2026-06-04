package com.jishuyang.springai.day01;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day1：5 分钟跑起第一个对话。
 * 测试：curl "http://localhost:8080/day1/chat?message=你好"
 */
@RestController
@RequestMapping("/day1")
public class ChatController {

    private final ChatClient chatClient;

    // 注入自动配置的 Builder，构建 ChatClient
    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(defaultValue = "用一句话介绍你自己") String message) {
        return chatClient
                .prompt(message)   // 用户问题
                .call()            // 同步调用
                .content();        // 取文本
    }
}
