package com.jishuyang.springai.day07;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day7：把自定义 Advisor 挂到 ChatClient。
 * 调用后看控制台日志，能看到请求/响应被打印。
 */
@RestController
@RequestMapping("/day7")
public class AdvisorController {

    private final ChatClient chatClient;

    public AdvisorController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(defaultValue = "你好") String message) {
        return chatClient.prompt(message).call().content();
    }
}
