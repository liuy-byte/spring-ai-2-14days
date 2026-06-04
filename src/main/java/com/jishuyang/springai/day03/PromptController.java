package com.jishuyang.springai.day03;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day3：Prompt 模板化与角色设定。
 */
@RestController
@RequestMapping("/day3")
public class PromptController {

    private final ChatClient chatClient;

    public PromptController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /** System 角色设定 */
    @GetMapping("/role")
    public String role(@RequestParam(defaultValue = "什么是依赖注入？") String message) {
        return chatClient.prompt()
                .system("你是一位资深 Java 架构师，回答简洁，必要时给代码示例")
                .user(message)
                .call().content();
    }

    /** User 模板参数 */
    @GetMapping("/code")
    public String code(@RequestParam(defaultValue = "Java") String language,
                       @RequestParam(defaultValue = "快速排序") String algorithm) {
        return chatClient.prompt()
                .user(u -> u.text("用 {language} 写一个 {algorithm}，附简短注释")
                        .param("language", language)
                        .param("algorithm", algorithm))
                .call().content();
    }

    /** System 模板参数：运行时设定语气 */
    @GetMapping("/voice")
    public String voice(@RequestParam(defaultValue = "讲个笑话") String message,
                        @RequestParam(defaultValue = "郭德纲") String voice) {
        return chatClient.prompt()
                .system(sp -> sp.text("你是一个聊天机器人，请用 {voice} 的语气回答").param("voice", voice))
                .user(message)
                .call().content();
    }
}
