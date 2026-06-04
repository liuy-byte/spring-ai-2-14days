package com.jishuyang.springai.day06;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day6：给 AI 装上记忆。
 * 测试同一 conversationId 多轮：
 *   /day6/chat?message=我叫詹姆斯，记住了&conversationId=u1
 *   /day6/chat?message=我叫什么名字？&conversationId=u1
 */
@RestController
@RequestMapping("/day6")
public class MemoryController {

    private final ChatClient chatClient;

    public MemoryController(ChatClient.Builder builder, ChatMemory chatMemory) {
        this.chatClient = builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message,
                       @RequestParam(defaultValue = "user-007") String conversationId) {
        return chatClient.prompt()
                .user(message)
                // 不同 conversationId 隔离不同用户的记忆
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}
