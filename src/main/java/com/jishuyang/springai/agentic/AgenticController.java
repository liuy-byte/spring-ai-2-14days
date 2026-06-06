package com.jishuyang.springai.agentic;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 番外篇·Agent 编排：5 种 workflow 模式演示。
 * 关键认知：这些 workflow 类不是框架内置 API，全是 ChatClient + Java 控制流拼出来的。
 */
@RestController
@RequestMapping("/agentic")
public class AgenticController {

    private final ChatClient chatClient;

    public AgenticController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /** ① Chain：理解诉求 → 给处理方向 → 润色话术 */
    @GetMapping("/chain")
    public String chain(@RequestParam(defaultValue = "我买的卷发棒坏了，很生气") String message) {
        ChainWorkflow workflow = new ChainWorkflow(chatClient,
                "你是诉求分析助手，提炼用户的真实诉求，一句话。",
                "你是政策顾问，根据诉求给出处理方向。",
                "你是金牌客服，把上面的处理方向改写成礼貌、简洁的回复话术。");
        return workflow.chain(message);
    }

    /** ② Routing：账单/技术/通用 分流 */
    @GetMapping("/routing")
    public String routing(@RequestParam(defaultValue = "我上周被重复扣款了") String message) {
        Map<String, String> routes = Map.of(
                "billing", "你是账单专家，专处理扣费、退款问题。",
                "technical", "你是技术支持，专解决产品报错、使用故障。",
                "general", "你是通用客服，处理其他咨询。");
        return new RoutingWorkflow(chatClient).route(message, routes);
    }

    /** ③ Parallelization：批量工单分类 */
    @PostMapping("/parallel")
    public List<String> parallel(@RequestBody List<String> tickets) {
        return new ParallelizationWorkflow(chatClient)
                .parallel("给这条工单分类，只回：投诉 / 咨询 / 退货", tickets, 4);
    }

    /** ④ Orchestrator-Workers：复杂任务动态拆解 */
    @GetMapping("/orchestrate")
    public String orchestrate(@RequestParam(defaultValue = "帮我规划一次三天两夜的杭州周末游") String task) {
        return new OrchestratorWorkersWorkflow(chatClient).process(task);
    }

    /** ⑤ Evaluator-Optimizer：话术自评自改 */
    @GetMapping("/evaluate")
    public String evaluate(
            @RequestParam(defaultValue = "起草一条退货拒绝话术，要求礼貌、给替代方案、不超过3句") String task) {
        return new EvaluatorOptimizerWorkflow(chatClient).loop(task, 3);
    }
}
