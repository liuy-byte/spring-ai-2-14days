# Spring AI 2.x 14 天系列 · 配套代码

公众号「**技术洋的编程笔记**」**Spring AI 2.x 14 天系列**的配套可运行项目。

基于 **Spring AI 2.0-M6** + Spring Boot 3.4 + DeepSeek，从第一个对话一路搭到带记忆、RAG、工具调用的完整 AI 应用。

> ✅ 已在 JDK 21 + Maven 3.9.9 下 `mvn compile` 验证编译通过（Spring AI 2.0.0-M6）。

---

## 技术栈

- JDK 17+
- Spring Boot 3.4.1
- Spring AI 2.0.0-M6（里程碑预览版）
- DeepSeek（对话）+ 本地 Transformers（嵌入，离线零成本）

---

## 快速开始

```bash
# 1. 准备 DeepSeek API Key：https://platform.deepseek.com/
# 2. 设置环境变量
export DEEPSEEK_API_KEY=sk-你的key

# 3. 启动
mvn spring-boot:run
```

> ⚠️ 首次启动会自动下载本地嵌入模型（ONNX，约 100MB+），需联网一次，请耐心等待。

---

## 14 天索引

| Day | 主题 | 包 | 试一下 |
|-----|------|-----|--------|
| 1 | 第一个对话 | `day01` | `GET /day1/chat?message=你好` |
| 2 | ChatClient / 流式 | `day02` | `GET /day2/stream?message=写首诗` |
| 3 | Prompt 模板 / 角色 | `day03` | `GET /day3/code?language=Java&algorithm=快排` |
| 4 | 结构化输出 | `day04` | `GET /day4/films?actor=汤姆·汉克斯` |
| 5 | 多模态 | `day05` | `GET /day5/image`（需视觉模型 + 图片） |
| 6 | Chat Memory | `day06` | `GET /day6/chat?message=我叫张三&conversationId=u1` |
| 7 | Advisors | `day07` | `GET /day7/chat?message=你好`（看控制台日志） |
| 8 | Embedding / 向量库 | `day08` | `POST /day8/load` → `GET /day8/search?query=Spring AI` |
| 9 | 文档 ETL | `day09` | `POST /day9/ingest` |
| 10 | RAG | `day10` | 先 `POST /day9/ingest` → `GET /day10/ask?question=退货政策几天` |
| 11 | 模块化 RAG | `day11` | 先 `POST /day9/ingest` → `GET /day11/ask?question=2.0有什么变化` |
| 12 | Tool Calling | `day12` | `GET /day12/ask?message=现在几点？北京天气如何` |
| 13 | MCP | `day13` | 需配 MCP server，见下方 |
| 14 | 全景图谱 + 面试 | — | 见公众号文章 |

---

## 重要提示（DeepSeek 能力边界）

- **Day5 多模态**：DeepSeek 不支持图像输入。需换视觉模型（gpt-4o / qwen-vl / GLM-4V / Ollama llava）——换对应 starter + 配 key，Media 代码不变；并在 `src/main/resources/images/` 放一张 `sample.png`。
- **Day8–11 嵌入**：DeepSeek 无嵌入模型，本项目用本地 Transformers（离线零成本）。
- **Day10 / 11 RAG**：提问前先 `POST /day9/ingest` 把示例知识库灌入向量库。
- **Day13 MCP**：默认不连接任何 server。在 `application.yml` 取消 MCP 配置注释、本机装好 `npx` 后启用。

---

## 关于版本

Spring AI 2.0 仍是**里程碑预览版**，少数 API / 包路径可能随版本微调。若 IDE 出现 import 红线，用「自动导入」修正即可（类名通常不变，仅包路径可能调整）。要上生产可改用 1.1.x GA 稳定版。

---

配套文章请见公众号「**技术洋的编程笔记**」Spring AI 2.x 14 天系列。
