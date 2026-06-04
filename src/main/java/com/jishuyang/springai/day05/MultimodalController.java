package com.jishuyang.springai.day05;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day5：多模态 —— 一个 ChatClient 同时读懂文字和图片。
 *
 * ⚠️ 注意：DeepSeek 当前不支持图像输入。运行此接口需切换到视觉模型
 *    （OpenAI gpt-4o / 通义 qwen-vl / 智谱 GLM-4V / 本地 Ollama llava）：
 *    换对应 starter + 配 api-key 即可，下面的 Media 代码一字不用改。
 *
 * 另需在 src/main/resources/images/ 放一张 sample.png。
 */
@RestController
@RequestMapping("/day5")
public class MultimodalController {

    private final ChatClient chatClient;

    public MultimodalController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/image")
    public String image() {
        return chatClient.prompt()
                .user(u -> u.text("这张图里有什么？")
                        .media(MimeTypeUtils.IMAGE_PNG, new ClassPathResource("/images/sample.png")))
                .call()
                .content();
    }
}
