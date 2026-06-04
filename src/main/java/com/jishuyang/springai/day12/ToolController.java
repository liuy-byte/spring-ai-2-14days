package com.jishuyang.springai.day12;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day12：Tool Calling —— 把工具交给模型，模型自动决定何时调用。
 * 测试：/day12/ask?message=现在几点了？顺便告诉我北京天气
 */
@RestController
@RequestMapping("/day12")
public class ToolController {

    private final ChatClient chatClient;

    public ToolController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/ask")
    public String ask(@RequestParam(defaultValue = "现在几点了？顺便告诉我北京天气") String message) {
        return chatClient.prompt()
                .user(message)
                .tools(new DateTimeTools(), new WeatherService())
                .call()
                .content();
    }
}
