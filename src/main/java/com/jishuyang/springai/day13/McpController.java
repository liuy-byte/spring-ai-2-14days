package com.jishuyang.springai.day13;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Day13：MCP —— 接入现成工具生态。
 *
 * ⚠️ 需先在 application.yml 取消 MCP 配置注释、接好一个 MCP server，
 *    否则没有可用工具（接口仍能启动，但模型无 MCP 工具可调）。
 *
 * ToolCallbackProvider 由 spring-ai-starter-mcp-client 自动装配；
 * 这里用 ObjectProvider 做空安全，保证未配置 MCP 时应用也能正常启动。
 */
@RestController
@RequestMapping("/day13")
public class McpController {

    private final ChatClient chatClient;

    public McpController(ChatClient.Builder builder,
                         ObjectProvider<ToolCallbackProvider> mcpToolsProvider) {
        ToolCallbackProvider mcpTools = mcpToolsProvider.getIfAvailable();
        this.chatClient = (mcpTools != null)
                ? builder.defaultToolCallbacks(mcpTools).build()
                : builder.build();
    }

    @GetMapping("/ask")
    public String ask(@RequestParam(defaultValue = "列出 /tmp 目录下的文件") String message) {
        return chatClient.prompt().user(message).call().content();
    }
}
