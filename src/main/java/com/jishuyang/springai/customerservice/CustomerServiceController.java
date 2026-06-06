package com.jishuyang.springai.customerservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 番外篇·生产加固版智能客服「小慧」。
 * 集成：记忆(Day6) + RAG(Day10) + Tool(Day12) + Advisor(Day7)。
 * 生产特性：容错兜底、token 用量记录、内容安全(敏感词)、会话隔离。
 *
 * 提问前先 POST /day9/ingest 灌入示例知识库。
 * 试一下：/cs/chat?message=你们退货政策几天？&userId=u1
 *
 * 说明：更重的生产特性（记忆落库 JdbcChatMemoryRepository、内容审核 Moderation、
 * Micrometer 指标）需引入额外依赖，本类用配置示例与注释指明落地方式，详见 application.yml。
 */
@RestController
@RequestMapping("/cs")
public class CustomerServiceController {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceController.class);

    private final ChatClient customerService;

    public CustomerServiceController(ChatClient.Builder builder,
                                     ChatMemory chatMemory,
                                     VectorStore vectorStore) {
        this.customerService = builder
                .defaultSystem("你是电商客服「小慧」，热情简洁，只依据知识库和工具结果回答，绝不编造。")
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),   // 记忆
                        QuestionAnswerAdvisor.builder(vectorStore).build(),     // RAG
                        new SimpleLoggerAdvisor(),                              // 日志
                        SafeGuardAdvisor.builder()                              // 内容安全：敏感词
                                .sensitiveWords(List.of("银行卡密码", "身份证号"))
                                .build())
                .defaultTools(new OrderTools())                                 // 业务工具
                .build();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message,
                       @RequestParam(defaultValue = "u1") String userId) {
        try {
            ChatResponse resp = customerService.prompt()
                    .user(message)
                    // conversationId 用 userId 隔离每个用户的记忆
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                    .call()
                    .chatResponse();

            // 记录 token 用量，盯成本（生产接 Micrometer 上报为指标）
            Usage usage = resp.getMetadata().getUsage();
            log.info("用户 {} 本次 token：prompt={} completion={} total={}",
                    userId, usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());

            return resp.getResult().getOutput().getText();
        } catch (Exception e) {
            // 容错兜底：模型异常绝不糊到用户脸上，转人工
            log.error("模型调用失败 user={}", userId, e);
            return "抱歉，这个问题我帮你转接人工客服～";
        }
    }
}
