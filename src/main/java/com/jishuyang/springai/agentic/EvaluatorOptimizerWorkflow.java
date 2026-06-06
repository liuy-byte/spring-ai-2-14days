package com.jishuyang.springai.agentic;

import org.springframework.ai.chat.client.ChatClient;

/**
 * 模式⑤·Evaluator-Optimizer：生成 → 评估 → 改进，循环逼近高质量结果。
 * 适合有明确好坏标准、值得反复打磨的任务，如敏感话术、合规文案。
 */
public class EvaluatorOptimizerWorkflow {

    private final ChatClient chatClient;

    public EvaluatorOptimizerWorkflow(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /** 评估结果：是否通过 + 改进建议（结构化输出） */
    public record Evaluation(boolean pass, String feedback) {
    }

    public String loop(String task, int maxRounds) {
        // 先生成初版
        String solution = chatClient.prompt().user(task).call().content();

        for (int i = 0; i < maxRounds; i++) {
            // 自评：是否达标 + 给改进建议
            Evaluation eval = chatClient.prompt()
                    .user("评估下面的回答是否满足要求「" + task + "」。"
                            + "返回 pass(true/false) 和 feedback 改进建议。\n回答：" + solution)
                    .call()
                    .entity(Evaluation.class);

            if (eval.pass()) {
                break;   // 达标，提前结束循环
            }
            // 带着反馈重写
            solution = chatClient.prompt()
                    .user("按反馈改进回答。\n任务：" + task
                            + "\n上版回答：" + solution
                            + "\n改进建议：" + eval.feedback())
                    .call()
                    .content();
        }
        return solution;
    }
}
