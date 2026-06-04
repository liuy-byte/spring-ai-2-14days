package com.jishuyang.springai.day02;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * Day2：吃透 ChatClient —— call 与 stream。
 */
@RestController
@RequestMapping("/day2")
public class ChatClientController {

    private final ChatClient chatClient;

    public ChatClientController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /** 同步：只取文本 */
    @GetMapping("/chat")
    public String chat(@RequestParam(defaultValue = "讲个笑话") String message) {
        return chatClient.prompt().user(message).call().content();
    }

    /** 同步：取完整响应，含 token 用量 */
    @GetMapping("/usage")
    public Map<String, Object> usage(@RequestParam(defaultValue = "讲个笑话") String message) {
        ChatResponse resp = chatClient.prompt().user(message).call().chatResponse();
        Usage usage = resp.getMetadata().getUsage();
        return Map.of(
                "reply", resp.getResult().getOutput().getText(),
                "totalTokens", usage.getTotalTokens());
    }

    /** 流式：打字机效果，返回 Flux */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam(defaultValue = "写一首关于秋天的短诗") String message) {
        return chatClient.prompt(message).stream().content();
    }

    /** 单次请求覆盖模型参数（温度压低，输出更确定） */
    @GetMapping("/options")
    public String options(@RequestParam(defaultValue = "写一个线程安全的单例") String message) {
        return chatClient.prompt().user(message)
                .options(ChatOptions.builder().temperature(0.2).build())
                .call().content();
    }
}
