package com.jishuyang.springai.customerservice;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 多模态客服：用 MiniMax-M3（OpenAI 兼容通道）识别用户发来的图——商品瑕疵、订单截图、物流面单。
 *
 * 跑通：① export MINIMAX_API_KEY=xxx
 *       ② src/main/resources/images/ 放一张 sample.png
 *       ③ GET /cs/chat-image?message=这张图里的商品有什么问题？
 *
 * baseUrl 填域名根 https://api.minimax.io，Spring AI 自动拼 /v1/chat/completions。
 * 手动构造 ChatModel 而非引 starter，避免容器出现两个 ChatModel Bean 导致主线装配冲突。
 */
@RestController
@RequestMapping("/cs")
public class VisionCustomerServiceController {

    private volatile ChatClient visionClient;

    private ChatClient visionClient() {
        if (visionClient == null) {
            synchronized (this) {
                if (visionClient == null) {
                    OpenAiChatModel model = OpenAiChatModel.builder()
                            .options(OpenAiChatOptions.builder()
                                    .baseUrl("https://api.minimax.io")
                                    .apiKey(System.getenv("MINIMAX_API_KEY"))
                                    .model("MiniMax-M3")
                                    .build())
                            .build();
                    visionClient = ChatClient.create(model);
                }
            }
        }
        return visionClient;
    }

    @GetMapping("/chat-image")
    public String chatImage(@RequestParam(defaultValue = "这张图里的商品有什么问题？") String message) {
        return visionClient().prompt()
                .user(u -> u.text(message)
                        .media(MimeTypeUtils.IMAGE_PNG, new ClassPathResource("/images/sample.png")))
                .call()
                .content();
    }
}
