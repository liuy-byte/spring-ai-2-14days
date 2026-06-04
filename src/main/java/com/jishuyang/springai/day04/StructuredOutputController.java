package com.jishuyang.springai.day04;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Day4：结构化输出 —— 让大模型直接吐 Java 对象。
 */
@RestController
@RequestMapping("/day4")
public class StructuredOutputController {

    private final ChatClient chatClient;

    public StructuredOutputController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public record ActorFilms(String actor, List<String> movies) {}

    public enum Sentiment { POSITIVE, NEUTRAL, NEGATIVE }

    /** 映射成单个对象 */
    @GetMapping("/films")
    public ActorFilms films(@RequestParam(defaultValue = "汤姆·汉克斯") String actor) {
        return chatClient.prompt()
                .user("生成 " + actor + " 出演的 5 部电影")
                .call()
                .entity(ActorFilms.class);
    }

    /** 映射成集合（用 ParameterizedTypeReference 绕过泛型擦除） */
    @GetMapping("/films-list")
    public List<ActorFilms> filmsList() {
        return chatClient.prompt()
                .user("生成汤姆·汉克斯和比尔·默瑞各自的 5 部电影")
                .call()
                .entity(new ParameterizedTypeReference<List<ActorFilms>>() {});
    }

    /** 枚举：分类任务 */
    @GetMapping("/sentiment")
    public Sentiment sentiment(@RequestParam(defaultValue = "物流太慢，再也不买了") String comment) {
        return chatClient.prompt()
                .user("判断这条评论的情感：'" + comment + "'")
                .call()
                .entity(Sentiment.class);
    }
}
