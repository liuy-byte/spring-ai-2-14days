package com.jishuyang.springai.customerservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 番外篇·生产加固版智能客服「小慧」。
 *
 * 接口：
 *   GET  /cs/chat        纯文字对话（DeepSeek，带记忆/RAG/Tool）
 *   GET  /cs/chat-image  识图对话（MiniMax-M3，固定 sample.png 演示）
 *   POST /cs/ask         统一入口：有图→MiniMax-M3，无图→DeepSeek
 *
 * 启动前：export DEEPSEEK_API_KEY=xxx；识图功能额外需 export MINIMAX_API_KEY=xxx
 * 先 POST /day9/ingest 灌入示例知识库，再调 /cs/chat 或 /cs/ask。
 */
@RestController
@RequestMapping("/cs")
public class CustomerServiceController {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceController.class);

    private final ChatClient customerService;
    private final ChatMemory chatMemory;

    public CustomerServiceController(ChatClient.Builder builder,
                                     ChatMemory chatMemory,
                                     VectorStore vectorStore) {
        this.chatMemory = chatMemory;
        this.customerService = builder
                .defaultSystem("你是电商客服「小慧」，热情简洁，只依据知识库和工具结果回答，绝不编造。")
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore).build(),
                        new SimpleLoggerAdvisor(),
                        SafeGuardAdvisor.builder()
                                .sensitiveWords(List.of("银行卡密码", "身份证号"))
                                .build())
                .defaultTools(new OrderTools())
                .build();
    }

    // ── 文字对话 ──────────────────────────────────────────────────────────────

    @GetMapping("/chat")
    public String chat(@RequestParam String message,
                       @RequestParam(defaultValue = "u1") String userId) {
        try {
            ChatResponse resp = customerService.prompt()
                    .user(message)
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                    .call()
                    .chatResponse();
            Usage usage = resp.getMetadata().getUsage();
            log.info("用户 {} token：{}", userId, usage.getTotalTokens());
            return resp.getResult().getOutput().getText();
        } catch (Exception e) {
            log.error("模型调用失败 user={}", userId, e);
            return "抱歉，这个问题我帮你转接人工客服～";
        }
    }

    // ── 识图对话（演示用，固定 sample.png）────────────────────────────────────

    @GetMapping("/chat-image")
    public String chatImage(@RequestParam(defaultValue = "这张图里的商品有什么问题？") String message) {
        return visionClient().prompt()
                .user(u -> u.text(message)
                        .media(MimeTypeUtils.IMAGE_PNG, new ClassPathResource("/images/sample.png")))
                .call()
                .content();
    }

    // ── 统一入口：有图→MiniMax-M3，无图→DeepSeek ─────────────────────────────

    @PostMapping("/ask")
    public String ask(@RequestParam String message,
                      @RequestParam(defaultValue = "u1") String userId,
                      @RequestParam(required = false) List<MultipartFile> images) throws IOException {
        List<MultipartFile> validImages = images == null ? List.of() :
                images.stream().filter(f -> f != null && !f.isEmpty()).toList();

        if (!validImages.isEmpty()) {
            // 预先取出所有字节（lambda 内不能抛受检异常）；ContentType 缺省按 image/jpeg 处理
            record MediaItem(String mimeType, byte[] bytes) {}
            List<MediaItem> items = new ArrayList<>();
            for (MultipartFile f : validImages) {
                String mime = f.getContentType() != null ? f.getContentType() : "image/jpeg";
                items.add(new MediaItem(mime, f.getBytes()));
            }
            try {
                String answer = visionClient().prompt()
                        .user(u -> {
                            u.text(message);
                            for (MediaItem item : items) {
                                u.media(MimeTypeUtils.parseMimeType(item.mimeType()),
                                        new ByteArrayResource(item.bytes()));
                            }
                        })
                        .call().content();

                // 图片对话以文本摘要写入共享记忆，后续文字路径（DeepSeek）可读取上下文
                chatMemory.add(userId, new UserMessage("【图片消息】" + message));
                chatMemory.add(userId, new AssistantMessage(answer));
                return answer;
            } catch (Exception e) {
                log.error("图片识别失败 user={}", userId, e);
                return "抱歉，图片暂时无法识别，请描述一下商品问题，我来帮您处理～";
            }
        }
        return chat(message, userId);
    }

    // ── MiniMax-M3 客户端（懒加载，未配 MINIMAX_API_KEY 时不影响主线）────────

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
}
